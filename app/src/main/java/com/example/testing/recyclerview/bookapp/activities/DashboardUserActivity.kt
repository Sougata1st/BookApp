package com.example.testing.recyclerview.bookapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.testing.recyclerview.bookapp.BooksUserFragment
import com.example.testing.recyclerview.bookapp.databinding.ActivityDashboardUserBinding
import com.example.testing.recyclerview.bookapp.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        checkuser()
        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        //init List
        categoryArrayList = ArrayList()

        //load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Catagories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                //load some static categories e.g. All, Most Viewed , Most Downloaded
                //Add data to Models
                val modelAll = ModelCategory("01", "All", 1, "")
                val modelMostViewed = ModelCategory("01", "Most Viewed", 1, "")
                val modelMostDownloaded = ModelCategory("01", "Most Downloaded", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                //add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.catagory}",
                        "${modelAll.uid}"
                    ), modelAll.catagory
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.catagory}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.catagory
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.catagory}",
                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.catagory
                )

                //refresh List
                viewPagerAdapter.notifyDataSetChanged()

                //Now load from Firebase db
                for (ds in snapshot.children){
                    //get data in Model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.catagory}",
                            "${model.uid}"
                        ), model.catagory
                    )
                    //refresh List
                    viewPagerAdapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        //setup adapter to viewpager
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behaviour: Int, context: Context) :
        FragmentPagerAdapter(fm, behaviour) {

        //holds list of fragments i.e. new instances for same fragment for each category
        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()

        //List of Title of categories, for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            //add fragment that will be passed as parameter in fragmentlist
            fragmentsList.add(fragment)
            //add title that will be passed as perameter
            fragmentTitleList.add(title)
        }

    }

    private fun checkuser() {
        val FirebaseUser = firebaseAuth.currentUser
        if (FirebaseUser != null) {
            binding.subTitleTv.text = FirebaseUser.email
            binding.profileBtn.visibility = View.VISIBLE
            binding.logOutBtn.visibility = View.VISIBLE
        } else {
            binding.subTitleTv.text = "Not Logged In"
            binding.profileBtn.visibility = View.GONE
            binding.logOutBtn.visibility = View.GONE
        }
    }

    fun LogoutClicked(view: View) {
        firebaseAuth.signOut()
        checkuser()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    fun manageProfileClicked(view: View) {
        startActivity(Intent(this,ProfileActivity::class.java))
    }
}