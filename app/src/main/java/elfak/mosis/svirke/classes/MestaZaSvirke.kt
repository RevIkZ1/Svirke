package elfak.mosis.svirke.classes

import java.util.Date
import java.util.UUID

data class MestaZaSvirke(
    var id: String = UUID.randomUUID().toString(),
    var title: String = "", // Naziv nastupa
    var vrstaMuzike: String = "",
    var description: String = "", // Opis ovde ce pisati gde se mogu kupiti karte
    var ownerId: String = "", // Ko je postavio oglas
    var date: Date,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var imageURL: String = "",
    var like: Int = 0,
    var dislike: Int = 0,
    var likedByUsers: MutableList<String> = mutableListOf(),
    var dislikedByUsers: MutableList<String> = mutableListOf()
) {
    constructor() : this("", "", "", "", "", Date(), 0.0, 0.0, "", 0, 0, mutableListOf(), mutableListOf())
}