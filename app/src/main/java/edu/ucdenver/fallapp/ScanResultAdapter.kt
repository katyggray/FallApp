package edu.ucdenver.fallapp

// import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.ucdenver.fallapp.databinding.ActivityMainBinding
import edu.ucdenver.fallapp.databinding.ListItemScanBinding
import kotlinx.android.synthetic.main.list_item_scan.view.*

// import kotlinx.android.synthetic.main.list_item_scan.view.

class ScanResultAdapter(
    // private val pContext: Context,
    private val items: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemScanBinding.inflate(inflater,parent,false)
        return ScanViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        // val item = items[position]
        // holder.bind(items[position], onClickListener)
        (holder as ScanViewHolder).bind(items[position], onClickListener)
    }


    inner class ScanViewHolder(val binding: ListItemScanBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ScanResult, onClickListener: (device: ScanResult) -> Unit) {
                binding.deviceName.text = item.device.name
                binding.macAddress.text = item.device.address
                // binding.signalStrength.text = item.result.device.getRssi().toString()
                binding.root.setOnClickListener { onClickListener(item) }
            }
        }
}