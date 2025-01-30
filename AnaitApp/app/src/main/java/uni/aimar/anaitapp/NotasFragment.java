package uni.aimar.anaitapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import uni.aimar.anaitapp.supabase.logIn.SupabaseClient;

public class NotasFragment extends Fragment {
    private SupabaseClient supabaseClient;
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notas, container, false);
        supabaseClient = new SupabaseClient();

        CalendarView calendarView = view.findViewById(R.id.calendarViewNotas);
        EditText noteText = view.findViewById(R.id.noteText);
        Button btnSaveNote = view.findViewById(R.id.btnSaveNote);
        TextView savedNote = view.findViewById(R.id.savedNote);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            loadNoteForDate(savedNote);
        });

        btnSaveNote.setOnClickListener(v -> saveNoteForDate(noteText.getText().toString()));

        return view;
    }

    private void saveNoteForDate(String note) {
        if (selectedDate.isEmpty()) {
            showToast("Selecciona una fecha");
            return;
        }

        supabaseClient.registerNote(selectedDate, note, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> showToast("Nota guardada exitosamente"));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> showToast("Error al guardar nota: " + error));
            }
        });
    }

    private void loadNoteForDate(TextView savedNote) {
        if (selectedDate.isEmpty()) {
            return;
        }

        supabaseClient.getNoteForDate(selectedDate, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> savedNote.setText(response));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> showToast("Error al cargar nota: " + error));
            }
        });
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            new Handler(Looper.getMainLooper()).post(action);
        }
    }
}