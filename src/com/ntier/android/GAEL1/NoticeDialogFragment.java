// http://developer.android.com/guide/topics/ui/dialogs.html
package com.ntier.android.GAEL1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class NoticeDialogFragment extends DialogFragment{


/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
public interface NoticeDialogListener{
	public void onDialogPositiveClick( DialogFragment dialog );
	//public void onDialogNegativeClick( DialogFragment dialog );
}//interface NoticeDialogListener

// Use this instance of the interface to deliver action events
NoticeDialogListener	mListener;

@Override
public Dialog onCreateDialog( final Bundle savedInstanceState ){
	// Build the dialog and set up the button click handler
	AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
	builder.setMessage( "Please Enable Network DATA" )
				.setPositiveButton( "OK", new DialogInterface.OnClickListener(){
					@Override
					public void onClick( final DialogInterface dialog, final int id ){
						// Send the positive button event back to the host activity
						mListener.onDialogPositiveClick( NoticeDialogFragment.this );
					}
				} );
	return builder.create();
}


// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
@Override
public void onAttach( final Activity activity ){
	super.onAttach( activity );
	// Verify that the host activity implements the callback interface
	try{
		// Instantiate the NoticeDialogListener so we can send events to the host
		mListener = (NoticeDialogListener)activity;
	}
	catch( ClassCastException e ){
		// The activity doesn't implement the interface, throw exception
		throw new ClassCastException( activity.toString()
												+ " must implement NoticeDialogListener" );
	}
}//onAttach

}//class NoticeDialogFragment
