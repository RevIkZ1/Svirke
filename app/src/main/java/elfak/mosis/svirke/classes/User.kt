package elfak.mosis.svirke.classes

data class User(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
    val phoneNumber: String,
    var imageURl:String="",
    var rang:Int=0,
    var points: Int =0
)
{
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        0,
    )
}