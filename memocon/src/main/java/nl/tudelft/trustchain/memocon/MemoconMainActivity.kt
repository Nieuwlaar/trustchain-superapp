package nl.tudelft.trustchain.memocon

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*
import nl.tudelft.trustchain.common.BaseActivity
import nl.tudelft.trustchain.memocon.ui.connect.ConnectFragment
import nl.tudelft.trustchain.memocon.ui.contacts.ContactsFragment
import nl.tudelft.trustchain.memocon.ui.groups.GroupsFragment
import nl.tudelft.trustchain.valuetransfer.passport.PassportHandler


class MemoconMainActivity : BaseActivity() {
    override val navigationGraph get() = R.navigation.memocon_navigation_graph
    lateinit var passportHandler: PassportHandler
//    val parentActivity: ValueTransferMainActivity by lazy {
//        requireActivity() as ValueTransferMainActivity
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)

        val ConnectFragment = ConnectFragment()
        val ContactsFragment = ContactsFragment()
        val GroupsFragment = GroupsFragment()

        makeCurrentFragment(ContactsFragment)

        nav_view.setOnNavigationItemSelectedListener {
            when (it.itemId){
                R.id.navigation_contacts -> makeCurrentFragment(ContactsFragment)
                R.id.navigation_groups -> makeCurrentFragment(GroupsFragment)
                R.id.navigation_connect -> makeCurrentFragment(ConnectFragment)
            }
            true
        }

//        val addIdentityBtn = findViewById<Button>(R.id.add_identity_btn)
        passportHandler = PassportHandler.getInstance(this)
//        val x = ValueTransferMainActivity()
//        addIdentityBtn.setOnClickListener{
//            IdentityOnboardingDialog().show(x, "test")
//            IdentityOnboardingDialog().show(x.fragmentManager, "test")
//            IdentityOnboardingDialog.startPassportScan("P")
//            IdentityOnboardingDialog.startPassportScan("P")
//        }
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}
