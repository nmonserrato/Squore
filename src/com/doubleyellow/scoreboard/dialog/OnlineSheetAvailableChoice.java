package com.doubleyellow.scoreboard.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import com.doubleyellow.android.view.ViewUtil;
import com.doubleyellow.scoreboard.R;
import com.doubleyellow.scoreboard.activity.ScoreSheetOnline;
import com.doubleyellow.scoreboard.main.ScoreBoard;
import com.doubleyellow.scoreboard.model.Model;

/**
 * Presented to the user after an online scoresheet of the match has been created.
 * User has a choice what to do with the link.
 */
public class OnlineSheetAvailableChoice extends BaseAlertDialog
{
    private enum OnlineSheetAction {
        ShareLink,
        Preview,
        ViewInBrowser,
        CopyToClipboard,
        DeleteFromServer,
    }

    public OnlineSheetAvailableChoice(Context context, Model matchModel, ScoreBoard scoreBoard) {
        super(context, matchModel, scoreBoard);
    }

    @Override public boolean storeState(Bundle outState) {
        outState.putString(OnlineSheetAvailableChoice.class.getSimpleName(), sShowURL);
        return true;
    }

    @Override public boolean init(Bundle outState) {
        init(outState.getString(OnlineSheetAvailableChoice.class.getSimpleName()));
        return true;
    }

    private String sShowURL = null;

    public void init(String sShareURL) {
        this.sShowURL = sShareURL;
    }

    @Override public void show() {
        adb.setTitle(context.getString(R.string.sb_online_sheet_available))
           .setIcon(android.R.drawable.ic_menu_share);

        mTranslateButtonToType.put(DialogInterface.BUTTON_POSITIVE, OnlineSheetAction.Preview);
        mTranslateButtonToType.put(DialogInterface.BUTTON_NEUTRAL , OnlineSheetAction.ShareLink);
        mTranslateButtonToType.put(DialogInterface.BUTTON_NEGATIVE, OnlineSheetAction.ViewInBrowser);

        for(int i = 0; i < mTranslateButtonToType.size(); i++) {
            int iButton = mTranslateButtonToType.keyAt(i);
            OnlineSheetAction type = mTranslateButtonToType.get(iButton);
            String text = ViewUtil.getEnumDisplayValue(context, R.array.OnlineSheetActionDisplayValues, type);
            switch (iButton) {
                case DialogInterface.BUTTON_POSITIVE: adb.setPositiveButton(text, chooseOnlineSheetAction); break;
                case DialogInterface.BUTTON_NEUTRAL : adb.setNeutralButton (text, chooseOnlineSheetAction); break;
                case DialogInterface.BUTTON_NEGATIVE: adb.setNegativeButton(text, chooseOnlineSheetAction); break;
            }
        }
        dialog = adb.show();
    }

    private final SparseArray<OnlineSheetAction> mTranslateButtonToType = new SparseArray<OnlineSheetAction>();

    private DialogInterface.OnClickListener chooseOnlineSheetAction = new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int which) {
            handleButtonClick(which);
        }
    };

    @Override public void handleButtonClick(int which) {
        OnlineSheetAction type = mTranslateButtonToType.get(which);
        switch (type) {
            case ShareLink:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, sShowURL);
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.cmd_share_with_friends)));
                break;
            case Preview:
                Intent nm = new Intent(context, ScoreSheetOnline.class);
                Bundle bundle = new Bundle();
                bundle.putString(ScoreSheetOnline.class.getSimpleName(), sShowURL );
                nm.putExtra(ScoreSheetOnline.class.getSimpleName(), bundle);
                context.startActivity(nm);
                break;
            case ViewInBrowser:
                Uri uriUrl = ScoreBoard.buildURL(context, sShowURL, false);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                context.startActivity(launchBrowser);
                break;
            case CopyToClipboard:
                // TODO:
                break;
            case DeleteFromServer:
                // TODO:
                break;
        }
        dialog.dismiss();
        if ( scoreBoard != null ) {
            // trigger a possible next dialog on the stack
            scoreBoard.triggerEvent(ScoreBoard.SBEvent.dialogClosed, this);
        }
    }
}
