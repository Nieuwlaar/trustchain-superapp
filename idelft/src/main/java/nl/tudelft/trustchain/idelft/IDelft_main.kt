package nl.tudelft.trustchain.idelft

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentManager
import nl.tudelft.trustchain.common.BaseActivity
import nl.tudelft.trustchain.valuetransfer.ValueTransferMainActivity
import nl.tudelft.trustchain.valuetransfer.dialogs.IdentityOnboardingDialog
import nl.tudelft.trustchain.valuetransfer.ui.VTFragment


class IDelft_main : BaseActivity() {
    override val navigationGraph get() = R.navigation.idelft_navigation_graph



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)
        val addIdentityBtn = findViewById<Button>(R.id.add_identity_btn)

//        val x = ValueTransferMainActivity()
        addIdentityBtn.setOnClickListener{
//            IdentityOnboardingDialog().startPassportScan("P")
//            IdentityOnboardingDialog().show(x.fragmentManager, "test")
        }
    }
}
