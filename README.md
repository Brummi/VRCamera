# VRCamera
Implementation of a VR camera for LibGDX. 
It is easy to use, works only with LibGDX libraries and cames with a head tracker.

The VRCamera class enables you to create VR applications with the LibGDX framework. 

The camera uses two different perspective cameras (one for each eye) and transforms them according to the current translation and rotation of the VRCamera.
The two rendered frames from the perspective cameras are rendered side by side on a batch. 
For rendering the eyes, the camera use the RendererForVR interface. It supplies a method in which you can render your 3D scene with the given perspective camera. The VRCamera calls this method twice each (for each eye / perspective camera) per render call.

For head tracking you can use the VRCameraInputAdapter class. 
It rotates the camera according to gyroscope / accelerometer values. It needs to be updated as often as possible to achieve the best results.
If you are using this headtracker, do not forget to enable the gyroscope in the AndroidLauncher class.

You can find a usage example <a href="https://github.com/Brummi/VRDemo">here</a>.
