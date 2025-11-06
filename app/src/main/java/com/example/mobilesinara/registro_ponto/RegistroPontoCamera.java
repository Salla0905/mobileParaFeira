package com.example.mobilesinara.registro_ponto;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroPontoCamera extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "RegistroPontoCamera";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Integer idUser;
    private IOperario api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView chamado");
        return inflater.inflate(R.layout.fragment_registro_ponto_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated chamado");

        previewView = view.findViewById(R.id.previewView);

        SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", getContext().MODE_PRIVATE);
        idUser = prefs.getInt("idUser", -1);
        Log.d(TAG, "idUser recuperado: " + idUser);

        if (idUser == -1) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
        }

        api = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);

        Button btTirarFoto = view.findViewById(R.id.bt_bater_ponto);
        btTirarFoto.setOnClickListener(v -> {
            Log.d(TAG, "Botão 'Bater Ponto' clicado");
            takePhoto();
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissão de câmera já concedida");
            startCamera();
        } else {
            Log.d(TAG, "Solicitando permissão de câmera");
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão de câmera concedida pelo usuário");
                startCamera();
            } else {
                Log.e(TAG, "Permissão de câmera negada");
                Toast.makeText(getContext(), "Permissão de câmera necessária", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        Log.d(TAG, "Iniciando câmera...");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "CameraProvider obtido");

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                Log.d(TAG, "Câmera iniciada com sucesso");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Erro ao iniciar câmera: ", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        Log.d(TAG, "takePhoto() chamado");

        if (imageCapture == null) {
            Log.e(TAG, "imageCapture está nulo — câmera não inicializada?");
            Toast.makeText(getContext(), "Câmera não pronta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idUser == null || idUser == -1) {
            Log.e(TAG, "idUser inválido: " + idUser);
            Toast.makeText(getContext(), "Usuário não definido", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(requireContext().getCacheDir(), "foto_temp.jpg");
        Log.d(TAG, "Arquivo de foto criado em: " + photoFile.getAbsolutePath());

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Foto salva com sucesso: " + photoFile.getAbsolutePath());
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        if (bitmap == null) {
                            Log.e(TAG, "Falha ao decodificar imagem — bitmap é nulo");
                        } else {
                            Log.d(TAG, "Bitmap carregado, enviando para backend...");
                            sendImageToBackend(bitmap);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Erro ao capturar imagem: ", exception);
                        Toast.makeText(getContext(), "Erro ao salvar foto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendImageToBackend(Bitmap bitmap) {
        Log.d(TAG, "Enviando imagem para o backend...");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        Log.d(TAG, "Imagem comprimida (" + bitmapData.length + " bytes)");

        RequestBody idBody = RequestBody.create(String.valueOf(idUser), MediaType.parse("text/plain"));
        Map<String, RequestBody> map = new HashMap<>();
        map.put("userId", idBody);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "fotoTeste",
                "foto.jpg",
                RequestBody.create(bitmapData, MediaType.parse("image/jpeg"))
        );

        Log.d(TAG, "Chamando endpoint verificarReconhecimento...");
        Call<Boolean> call = api.verificarReconhecimento(map, filePart);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Log.d(TAG, "Resposta recebida: código " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    boolean reconhecido = response.body();
                    Log.d(TAG, "Reconhecido: " + reconhecido);

                    if (reconhecido) {
                        Toast.makeText(getContext(), "Reconhecimento OK", Toast.LENGTH_SHORT).show();
                        try {
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_registroPontoCamera_to_registroPontoConfirmar);
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao navegar: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Usuário não reconhecido. Tente novamente.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e(TAG, "Erro servidor: " + response.code() + " - " +
                                (response.errorBody() != null ? response.errorBody().string() : "sem corpo de erro"));
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler corpo de erro: ", e);
                    }
                    Toast.makeText(getContext(), "Erro no servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(TAG, "Falha na chamada Retrofit: ", t);
                Toast.makeText(getContext(), "Falha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
