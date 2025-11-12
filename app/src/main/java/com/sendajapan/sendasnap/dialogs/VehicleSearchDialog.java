package com.sendajapan.sendasnap.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

public class VehicleSearchDialog {

    public interface OnSearchListener {
        void onSearch(String searchType, String searchQuery);
    }

    public static class Builder {
        private final Context context;
        private OnSearchListener searchListener;
        private boolean cancelable = true;
        private final HapticFeedbackHelper hapticHelper;

        public Builder(@NonNull Context context) {
            this.context = context;
            this.hapticHelper = HapticFeedbackHelper.getInstance(context);
        }

        public Builder setOnSearchListener(OnSearchListener listener) {
            this.searchListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Dialog create() {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setContentView(R.layout.dialog_vehicle_search);
            dialog.setCancelable(cancelable);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.outline_box_shape);
            }

            Spinner spinnerSearchType = dialog.findViewById(R.id.spinnerSearchType);
            ImageView imgSpinnerArrow = dialog.findViewById(R.id.imgSpinnerArrow);
            TextInputLayout tilSearchQuery = dialog.findViewById(R.id.tilSearchQuery);
            TextInputEditText etSearchQuery = dialog.findViewById(R.id.etSearchQuery);
            MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
            MaterialButton btnSearch = dialog.findViewById(R.id.btnSearch);

            String[] searchTypes = {"Vehicle ID", "Chassis Number"};
            String[] searchTypeValues = {"vehicle_id", "veh_chassis_number"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    R.layout.spinner_item_search_type, searchTypes);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_bordered);
            spinnerSearchType.setAdapter(adapter);
            spinnerSearchType.setSelection(0);

            // Store selected value
            final int[] selectedIndex = {0};
            spinnerSearchType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    selectedIndex[0] = position;
                    hapticHelper.vibrateClick();
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });

            imgSpinnerArrow.setOnClickListener(v -> {
                spinnerSearchType.performClick();
                hapticHelper.vibrateClick();
            });

            btnCancel.setOnClickListener(v -> {
                hapticHelper.vibrateClick();
                dialog.dismiss();
            });

            btnSearch.setOnClickListener(v -> {
                String searchQuery = etSearchQuery.getText().toString().trim();

                tilSearchQuery.setError(null);

                if (searchQuery.isEmpty()) {
                    tilSearchQuery.setError("Search query is required");
                    etSearchQuery.requestFocus();
                    hapticHelper.vibrateError();
                    return;
                }

                if (searchQuery.length() < 3) {
                    tilSearchQuery.setError("Search query must be at least 3 characters");
                    etSearchQuery.requestFocus();
                    hapticHelper.vibrateError();
                    return;
                }

                hapticHelper.vibrateClick();
                dialog.dismiss();

                if (searchListener != null) {
                    String searchType = searchTypeValues[selectedIndex[0]];
                    searchListener.onSearch(searchType, searchQuery);
                }
            });

            return dialog;
        }

        public void show() {
            create().show();
        }
    }
}

