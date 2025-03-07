package com.linchisin.fingerprintlocalization.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.linchisin.fingerprintlocalization.R;

/**
 * Created by Sin on 2018/4/19.
 *
 *@author LinChiSin
 * @date 2018-4-25 下午 7:54:32
 * @description 检查权限工具（暂时无效）
 */

public class PermissionUtils {
	public static void showRequestPermissionDialog(final Context context, String message) {
		AlertDialog dialog = new AlertDialog.Builder(context).setPositiveButton(context.getString(R.string.go_setting), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//打开应用列表
				PermissionUtils.openAppSettingList(context);
				dialog.dismiss();
			}
		})
				.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(false)
				.setMessage(message)
				.setTitle(R.string.permission_title)
				.show();
		//设置按钮颜色
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
	}
	public static void openAppSettingList(Context context) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
