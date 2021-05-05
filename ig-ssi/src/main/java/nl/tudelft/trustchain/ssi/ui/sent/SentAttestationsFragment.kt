package nl.tudelft.trustchain.ssi.ui.sent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mattskala.itemadapter.ItemAdapter
import nl.tudelft.trustchain.common.ui.BaseFragment
import nl.tudelft.trustchain.common.util.viewBinding
import nl.tudelft.trustchain.ssi.Communication
import nl.tudelft.trustchain.ssi.R
import nl.tudelft.trustchain.ssi.databinding.FragmentSentAttestationsBinding

class SentAttestationsFragment : BaseFragment(R.layout.fragment_sent_attestations) {

    private val adapter = ItemAdapter()
    private val binding by viewBinding(FragmentSentAttestationsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.registerRenderer(
            SentItemRenderer(
                {
                    onRevoke(it)
                },
                {
                    onHold(it)
                }
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayout.VERTICAL
            )
        )
        binding.refreshLayout.setOnRefreshListener {
            loadEntries()
        }
        loadEntries()
    }

    private fun loadEntries() {
        val channel =
            Communication.load()
        val entries = channel.getAttributesSignedBy(channel.myPeer)
            .mapIndexed { index, blob -> SentItem(index, blob) }
        adapter.updateItems(entries)
        binding.imgEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onRevoke(sentItem: SentItem) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                requireContext(),
                "Revoking ${sentItem.attestation.attributeName}",
                Toast.LENGTH_LONG
            ).show()
        }
        val channel = Communication.load()
        channel.revoke(sentItem.attestation.metadata.hash)
    }

    private fun onHold(sentItem: SentItem) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                requireContext(),
                "Holding ${sentItem.index}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
