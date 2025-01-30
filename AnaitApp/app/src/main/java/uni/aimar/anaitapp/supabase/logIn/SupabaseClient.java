package uni.aimar.anaitapp.supabase.logIn;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SupabaseClient {
    private static final String BASE_URL = "https://posintlgvfpumbjsdlfm.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBvc2ludGxndmZwdW1ianNkbGZtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzgxMDAzMjQsImV4cCI6MjA1MzY3NjMyNH0.65lGHjOXRVNhns1lghVy5k8UsESZWpN0b36u4RQUJ5k";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public SupabaseClient() {
        this.client = new OkHttpClient();
    }

    public interface SupabaseCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    // Registro de usuario
    public void registerUser(String email, String password, SupabaseCallback callback) {
        String url = BASE_URL + "usuarios";

        try {
            JSONObject json = new JSONObject()
                    .put("email", email)
                    .put("pass", password);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al crear usuario: " + e.getMessage());
        }
    }

    // Login de usuario
    public void loginUser(String email, String password, SupabaseCallback callback) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());
            String url = BASE_URL + "usuarios?email=eq." + encodedEmail + "&pass=eq." + encodedPassword;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        if (responseBody.equals("[]")) {
                            callback.onFailure("Usuario o contraseña incorrectos");
                        } else {
                            callback.onSuccess(responseBody);
                        }
                    } else {
                        callback.onFailure("Error: " + response.code() + " " + response.message());
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error en el login: " + e.getMessage());
        }
    }

    // Registro y actualización de turnos
    public void registerShift(String date, boolean available, SupabaseCallback callback) {
        checkExistingShift(date, new SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                if (response.equals("[]")) {
                    createNewShift(date, available, callback);
                } else {
                    updateExistingShift(date, available, callback);
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure("Error al verificar turno: " + error);
            }
        });
    }

    private void createNewShift(String date, boolean available, SupabaseCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("fecha", date);
            json.put("disponible", available);
            json.put("estado", available ? "Disponible" : "No Disponible");
            json.put("hora_inicio", "09:00");
            json.put("hora_fin", "18:00");
            json.put("descripcion", available ? "Día laboral" : "Día no laboral");

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "turnos")
                    .post(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al crear turno: " + e.getMessage());
        }
    }

    private void updateExistingShift(String date, boolean available, SupabaseCallback callback) {
        try {
            String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.toString());
            JSONObject json = new JSONObject();
            json.put("disponible", available);
            json.put("estado", available ? "Disponible" : "No Disponible");
            json.put("descripcion", available ? "Día laboral" : "Día no laboral");

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "turnos?fecha=eq." + encodedDate)
                    .patch(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al actualizar turno: " + e.getMessage());
        }
    }

    private void checkExistingShift(String date, SupabaseCallback callback) {
        try {
            String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "turnos?fecha=eq." + encodedDate)
                    .get()
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al verificar turno: " + e.getMessage());
        }
    }

    // Eliminación de turnos
    public void deleteShift(String date, SupabaseCallback callback) {
        try {
            String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "turnos?fecha=eq." + encodedDate)
                    .delete()
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al eliminar turno: " + e.getMessage());
        }
    }

    // Manejo de notas
    public void registerNote(String date, String note, SupabaseCallback callback) {
        try {
            JSONObject json = new JSONObject()
                    .put("fecha", date)
                    .put("nota", note);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "notas")
                    .post(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al guardar nota: " + e.getMessage());
        }
    }

    public void getNoteForDate(String date, SupabaseCallback callback) {
        try {
            String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "notas?fecha=eq." + encodedDate)
                    .get()
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al obtener nota: " + e.getMessage());
        }
    }

    public void deleteNote(String date, SupabaseCallback callback) {
        try {
            String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "notas?fecha=eq." + encodedDate)
                    .delete()
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Prefer", "return=minimal")
                    .build();

            executeRequest(request, callback);
        } catch (Exception e) {
            callback.onFailure("Error al eliminar nota: " + e.getMessage());
        }
    }

    // Método común para ejecutar requests
    private void executeRequest(Request request, SupabaseCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess(responseBody);
                } else {
                    callback.onFailure("Error " + response.code() + ": " + responseBody);
                }
                response.close();
            }
        });
    }
}