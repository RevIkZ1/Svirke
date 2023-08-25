package elfak.mosis.svirke.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import elfak.mosis.svirke.R
import elfak.mosis.svirke.classes.User

class LeaderBoardAdapter(private val context:Context,private val userList: List<User>) : BaseAdapter() {


    override fun getCount(): Int {
        return userList.size
    }

    override fun getItem(p0: Int): Any {
        return userList[p0]
    }

    override fun getItemId(p0: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        val holder: ViewHolder

        if (myView == null) {
            myView = LayoutInflater.from(context).inflate(R.layout.custom_leader, parent, false)
            holder = ViewHolder(myView)
            myView.tag = holder
        } else {
            holder = myView.tag as ViewHolder
        }

        val user = userList[position]
        holder.usernameTextView.text = user.username
        holder.pointsTextView.text = user.points.toString()

        return myView!!
    }

    private class ViewHolder(view: View) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameLeaderboardTextView)
        val pointsTextView: TextView = view.findViewById(R.id.pointsLeaderboardTextView)
    }
}