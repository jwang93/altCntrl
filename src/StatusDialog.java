package altCntrl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class StatusDialog extends DialogFragment {

	private String status;
	private String statusString;

	public StatusDialog(boolean status) {
		this.status = status ? "ON" : "OFF";
		statusString = "<br/>altCntrl has been turned " + this.status + "<br/>";
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		TextView text = new TextView(getActivity());
		text.setText(Html.fromHtml(statusString));
		text.setGravity(Gravity.CENTER);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("altCntrl Status Update")
				.setView(text)
				.setNegativeButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		return builder.create();
	}
}
