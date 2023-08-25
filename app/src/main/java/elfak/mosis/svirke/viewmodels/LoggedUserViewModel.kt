package elfak.mosis.svirke.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import elfak.mosis.svirke.classes.User

class LoggedUserViewModel: ViewModel() {
    private val _user= MutableLiveData<User?>(null)
    private val _location= MutableLiveData<LatLng?>(null)
    var user
        get()=_user.value
        set(value){_user.value=value}

    var location
        get()=_location.value
        set(value){_location.value=value}
}