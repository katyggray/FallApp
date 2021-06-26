package edu.ucdenver.fallapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import edu.ucdenver.fallapp.databinding.FragmentBleScanBinding
import edu.ucdenver.fallapp.databinding.FragmentPatchControlBinding
import kotlinx.android.synthetic.main.fragment_ble_scan.*

private const val TAG = "BLEscan"

class BleScanFragment : Fragment() {

    private var _binding: FragmentBleScanBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val bleScanViewModel: BleScanViewModel by lazy {
        ViewModelProvider(this).get(BleScanViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"Scan results: ${bleScanViewModel.device_names.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentBleScanBinding.inflate(inflater,container,false)
        val view = binding.root

        return view
    }

    override fun onStart() {
        super.onStart()

        scan_button.setOnClickListener {
            //startBleScan()
            Log.d(TAG,"scan")
        }
    }

    private inner class ScanHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = itemView.findViewById(R.id.device_name)
    }

    companion object {
        fun newInstance(): BleScanFragment {
            return BleScanFragment()
        }
    }
}