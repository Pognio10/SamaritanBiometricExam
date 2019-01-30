# SAMARITAN
- Installare il file .apk su un dispositivo android (è ottimizzato solo per gli smartphone), puoi scaricare il file apk da [qui](/apk/app-release.apk).

- Dopo aver installato l'applicazione bisogno concedergli i permessi di superuser, Impostazioni -> Applicazioni -> Accesso Speciale -> Ammistratore Dispositivo.

- L'applicazione inizialmente non avrà nessun set di persone in whitelist, quindi bisogna creare l'oggetto persona e inserire le foto, in modo che il sistema effettui il training su quelle immagini, è consigliato di mettere almeno 10 foto e non più di 248. È necessario inserire foto di buona qualità in cui il viso è ben illuminato e in cui non compaio più volti.

![sample](/apk/photo_2019-01-30_11-55-44.jpg) ![sample](/apk/photo_2019-01-30_12-01-24.jpg)

- Una volta salvato le foto l'applicazione effettuerà il training dell'intero gruppo #### Whitelist

- Quando tutto è stato configurato il sistema potrà essere avviato,(basterà cliccare sul pulsante Active) l'applicazione ogni 15 secondi scatterà una foto dalla fotocamera frontale e la manderà al sevizio [azure](https://docs.microsoft.com/it-it/azure/cognitive-services/face/overview) per l'identificazione, il servizio ritornerà UUID della persona che riconosce con una confidance di assomiglianza.

-  Se il dispositivo quando riceve UUID della persona non la riconosce allora bloccherà il dispositivo.

- Per disattivare il servizio bisogna riaprire l'applicazione e cliccare sul pulsante "Deactive"




## Il sistema implemeta la libreria Android Hidden Camera per effettuare la cattura delle immagini in background 

### What is this library for?
This library allows application to take the picture using the device camera without showing the preview of it. Any application can capture the image from front or rear camera from the background service and this library will handle all the complexity on behalf of the application. You can capture images from activity, fragment and **even from the background service** using this library.

### Gradle Dependency:
```
dependencies {
    compile 'com.kevalpatel2106:hiddencamera:1.3.3'
}
```

### How to integrate?

Step-1: Inherit the builtin class.

|       Component       |              Class to inherit              |                             Sample                          |
|-----------------------|:------------------------------------------:|------------------------------------------------------------:|
|Activity               |com.androidhiddencamera.HiddenCameraActivity|`public class DemoCamActivity extends HiddenCameraActivity {`|
|Fragment               |com.androidhiddencamera.HiddenCameraFragment|`public class DemoCamFragment extends HiddenCameraFragment {`|
|Service                |com.androidhiddencamera.HiddenCameraService |`public class DemoCamService extends HiddenCameraService {`  |

Step-2: Create the camera configuration. In this developer can define which camera they want to use, output image format, capture image resolution etc parameters.

```
//Setting camera configuration
mCameraConfig = new CameraConfig()
    .getBuilder(getActivity())
    .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
    .setImageRotation(CameraRotation.ROTATION_270)
    .build();
```

Step-3: Start the camera in `onCreate()` by calling `startCamera(CameraConfig)`. Before starting the camera, ask user for the camera runtime permission.

```
//Check for the camera permission for the runtime
if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

    //Start camera preview
    startCamera(mCameraConfig);
} else {
    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 101);
}
```

- If you are capturing the image from the service, you have to check if the application has the draw ver other application permission or not? If the permission is not available, application can ask user to grat the permission using `HiddenCameraUtils.openDrawOverPermissionSetting()`.

```
if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
    if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
        ...
        ...
    } else {
        //Open settings to grant permission for "Draw other apps".
        HiddenCameraUtils.openDrawOverPermissionSetting(this);
    }
} else {
    //TODO Ask your parent activity for providing runtime permission
}
```

Step-4: Take image in background whenever you want by calling `takePicture()`. You will receive captured image file in `onImageCapture()` callback.

Step -5: Handle errors by overriding `onError()` callback. In this callback you will receive an error code for each error occurred. You can take specific actions based on the error code.

```
@Override
public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
    switch (errorCode) {
        case CameraError.ERROR_CAMERA_OPEN_FAILED:
            //Camera open failed. Probably because another application
            //is using the camera
            break;
        case CameraError.ERROR_IMAGE_WRITE_FAILED:
            //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
            break;
        case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
            //camera permission is not available
            //Ask for the camra permission before initializing it.
            break;
        case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
            //Display information dialog to the user with steps to grant "Draw over other app"
            //permission for the app.
            HiddenCameraUtils.openDrawOverPermissionSetting(this);
            break;
        case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
            Toast.makeText(this, "Your device does not have front camera.", Toast.LENGTH_LONG).show();
            break;
    }
}
```

##### That's it.

### Demo
- You can download the sample apk from [here](/apk/sample.apk).
- ![sample](/apk/sample.png)

### Contribute:
##### Simple 3 step to contribute into this repo:

1. Fork the project.
2. Make required changes and commit.
3. Generate pull request. Mention all the required description regarding changes you made.

### Questions
Hit me on twitter [![Twitter](https://img.shields.io/badge/Twitter-@kevalpatel2106-blue.svg?style=flat)](https://twitter.com/kevalpatel2106)

### License
Copyright 2017 Keval Patel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

