package uni.aimar.anaitapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import uni.aimar.anaitapp.supabase.logIn.SupabaseClient;

public class TurnosFragment extends Fragment {
    private SupabaseClient supabaseClient;
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_turnos, container, false);
        supabaseClient = new SupabaseClient();

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        Button btnWork = view.findViewById(R.id.btnWork);
        Button btnNotWork = view.findViewById(R.id.btnNotWork);
        Button btnClear = view.findViewById(R.id.btnClear);
        TextView selectedDateText = view.findViewById(R.id.selectedDateText);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            selectedDateText.setText("Día seleccionado: " + selectedDate);
        });

        btnWork.setOnClickListener(v -> updateShift(true));
        btnNotWork.setOnClickListener(v -> updateShift(false));
        btnClear.setOnClickListener(v -> deleteShift());

        return view;
    }

    private void updateShift(boolean available) {
        if (selectedDate.isEmpty()) {
            showToast("Selecciona una fecha");
            return;
        }

        supabaseClient.registerShift(selectedDate, available, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> showToast("Turno actualizado"));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> showToast("Error al actualizar turno: " + error));
            }
        });
    }

    private void deleteShift() {
        if (selectedDate.isEmpty()) {
            showToast("Selecciona una fecha");
            return;
        }

        supabaseClient.deleteShift(selectedDate, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> showToast("Turno eliminado"));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> showToast("Error al eliminar turno: " + error));
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