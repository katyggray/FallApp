package edu.ucdenver.fallapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import edu.ucdenver.fallapp.databinding.ActivityPatchFunctionBinding


private const val TAG = "PatchControl"

class PatchControlActivity : AppCompatActivity() {

/*    private var _binding: ActivityPatchFunctionBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!*/

    private lateinit var binding: ActivityPatchFunctionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatchFunctionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }


    override fun onStart() {
        super.onStart()

        binding.temperature.setOnClickListener{
            Log.d(TAG,"temp")
            // TODO: call characteristic to pull temperature
            // TODO: hex to dec conversion
        }
        binding.battery.setOnClickListener{
            Log.d(TAG,"batt")
            // TODO: call characteristic to pull battery level
            // TODO: hex to dec conversion
        }
        binding.bulkTransfer.setOnClickListener{
            Log.d(TAG,"bulk")
            // TODO: call characteristic to start bulk data transfer
            // TODO: save bulk data
        }
        binding.calibrate.setOnClickListener{
            Log.d(TAG,"cal")
            // TODO: characteristic call for setting calibration
        }

        binding.logSwitch.setOnCheckedChangeListener { switch, isChecked ->
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
        binding.rtmSwitch.setOnCheckedChangeListener { switch, isChecked ->
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