package engine.space;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface CameraInheritable {
	Vector3fc getPosition();
	Vector3fc getAttachedCameraOffset();
	Quaternionfc getRotation();
}
