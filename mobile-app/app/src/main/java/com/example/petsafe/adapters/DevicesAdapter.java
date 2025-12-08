package com.example.petsafe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petsafe.R;
import com.example.petsafe.models.Device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {

    private List<Device> deviceList;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onEditClick(Device device);
        void onDeleteClick(Device device);
        void onDeviceClick(Device device);
    }

    public DevicesAdapter(OnDeviceClickListener listener) {
        this.deviceList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_card, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setDevices(List<Device> devices) {
        this.deviceList = devices;
        notifyDataSetChanged();
    }

    public void addDevice(Device device) {
        this.deviceList.add(device);
        notifyItemInserted(deviceList.size() - 1);
    }

    public void updateDevice(Device updatedDevice) {
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getId().equals(updatedDevice.getId())) {
                deviceList.set(i, updatedDevice);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeDevice(Device device) {
        int position = deviceList.indexOf(device);
        if (position != -1) {
            deviceList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSerialNumber;
        private final TextView tvModel;
        private final TextView tvImei;
        private final TextView tvStatus;
        private final TextView tvLastComm;
        private final TextView tvLastLocation;
        private final ImageButton btnMenu;
        private final ImageView ivDeviceIcon;
        private final ImageView ivStatusIcon;
        private final LinearLayout llImei;
        private final LinearLayout llStatus;
        private final LinearLayout llLastComm;
        private final LinearLayout llLastLocation;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvModel = itemView.findViewById(R.id.tvModel);
            tvImei = itemView.findViewById(R.id.tvImei);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLastComm = itemView.findViewById(R.id.tvLastComm);
            tvLastLocation = itemView.findViewById(R.id.tvLastLocation);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            ivDeviceIcon = itemView.findViewById(R.id.ivDeviceIcon);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
            llImei = itemView.findViewById(R.id.llImei);
            llStatus = itemView.findViewById(R.id.llStatus);
            llLastComm = itemView.findViewById(R.id.llLastComm);
            llLastLocation = itemView.findViewById(R.id.llLastLocation);
        }

        public void bind(Device device, OnDeviceClickListener listener) {
            tvSerialNumber.setText(device.getSerialNumber());

            // Model
            if (device.getModel() != null && !device.getModel().isEmpty()) {
                tvModel.setText(device.getModel());
            } else {
                tvModel.setText("Modelo desconhecido");
            }

            // IMEI (optional field)
            if (device.getImei() != null && !device.getImei().isEmpty()) {
                tvImei.setText(device.getImei());
                llImei.setVisibility(View.VISIBLE);
            } else {
                llImei.setVisibility(View.GONE);
            }

            // Status
            if (device.getActive() != null) {
                if (device.getActive()) {
                    tvStatus.setText("Ativo");
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.success));
                    ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                    ivStatusIcon.setColorFilter(itemView.getContext().getColor(R.color.success));
                } else {
                    tvStatus.setText("Inativo");
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.error));
                    ivStatusIcon.setImageResource(R.drawable.ic_cancel);
                    ivStatusIcon.setColorFilter(itemView.getContext().getColor(R.color.error));
                }
                llStatus.setVisibility(View.VISIBLE);
            } else {
                llStatus.setVisibility(View.GONE);
            }

            // Last Communication (optional field)
            if (device.getLastComm() != null && !device.getLastComm().isEmpty()) {
                String formattedDate = formatDateTime(device.getLastComm());
                tvLastComm.setText(formattedDate);
                llLastComm.setVisibility(View.VISIBLE);
            } else {
                llLastComm.setVisibility(View.GONE);
            }

            // Last Location (optional field)
            if (device.getLastLatitude() != null && device.getLastLongitude() != null) {
                String location = String.format(Locale.getDefault(), "%.6f, %.6f",
                    device.getLastLatitude(), device.getLastLongitude());
                tvLastLocation.setText(location);
                llLastLocation.setVisibility(View.VISIBLE);
            } else {
                llLastLocation.setVisibility(View.GONE);
            }

            // Menu button
            btnMenu.setOnClickListener(v -> showPopupMenu(v, device, listener));

            // Card click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }

        private void showPopupMenu(View view, Device device, OnDeviceClickListener listener) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.device_item_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditClick(device);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteClick(device);
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private String formatDateTime(String dateTimeStr) {
            try {
                // Input format: ISO 8601 (e.g., "2024-01-15T14:30:00Z")
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                Date date = inputFormat.parse(dateTimeStr);
                if (date != null) {
                    // Calculate time ago
                    long diffMs = System.currentTimeMillis() - date.getTime();
                    long diffMinutes = diffMs / (60 * 1000);
                    long diffHours = diffMs / (60 * 60 * 1000);
                    long diffDays = diffMs / (24 * 60 * 60 * 1000);

                    if (diffMinutes < 60) {
                        return "Há " + diffMinutes + " min";
                    } else if (diffHours < 24) {
                        return "Há " + diffHours + " h";
                    } else if (diffDays < 7) {
                        return "Há " + diffDays + " dias";
                    } else {
                        return outputFormat.format(date);
                    }
                }
            } catch (ParseException e) {
                // If parsing fails, return original string
                return dateTimeStr;
            }
            return dateTimeStr;
        }
    }
}
