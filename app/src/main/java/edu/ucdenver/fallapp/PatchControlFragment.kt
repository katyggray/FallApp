package edu.ucdenver.fallapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.ucdenver.fallapp.databinding.FragmentPatchControlBinding
import kotlinx.android.synthetic.main.fragment_patch_control.*

private const val TAG = "PatchControl"

class PatchControlFragment : Fragment() {

    private var _binding: FragmentPatchControlBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatchControlBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()

        _binding!!.temperature.setOnClickListener{
            Log.d(TAG,"temp")
            // TODO: call characteristic to pull temperature
            // TODO: hex to dec conversion
        }
        _binding!!.battery.setOnClickListener{
            Log.d(TAG,"batt")
            // TODO: call characteristic to pull battery level
            // TODO: hex to dec conversion
        }
        _binding!!.bulkTransfer.setOnClickListener{
            Log.d(TAG,"bulk")
            // TODO: call characteristic to start bulk data transfer
            // TODO: save bulk data
        }
        _binding!!.calibrate.setOnClickListener{
            Log.d(TAG,"cal")
            // TODO: characteristic call for setting calibration
        }

        _binding!!.logSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if (isChecked) {
                Log.d(TAG, "toglog on")
                // TODO: dec/binary for data logging on
                // TODO: write log on to BLE
            } else {
                Log.d(TAG, "toglog off")
                // TODO: dec/binary for data logging off
                // TODO: write log off to BLE
            }
        }
            // Log.d(TAG,"toglog") }
        _binding!!.rtmSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if (isChecked) {
                Log.d(TAG, "rtmtog on")
                // TODO: dec/binary for rtm on
                // TODO: write rtm on to BLE
            } else {
                Log.d(TAG, "rtmtog off")
                // TODO: dec/binary for rtm off
                // TODO: write rtm off to BLE
            }
            // Log.d(TAG,"rtmlog") }
        }
    }

}