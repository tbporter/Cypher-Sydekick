package com.github.tbporter.cypher_sydekick.chat;

import com.github.tbporter.cypher_sydekick.*;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Custom adapter for the chat conversation that shows an icon for person, has a
 * subtitle, and actual content of the message.
 * 
 * @author teja
 * 
 */
public class ConversationAdapter extends BaseAdapter {
	private ArrayList<ConversationItem> conversationItemArray;

	private LayoutInflater mInflater;

	public ConversationAdapter(Context context,
			ArrayList<ConversationItem> convItems) {
		conversationItemArray = convItems;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return conversationItemArray.size();
	}

	public Object getItem(int position) {
		return conversationItemArray.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = null;
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.chat_conversation_item,
					null);
			holder = new ViewHolder();
			holder.message = (TextView) convertView
					.findViewById(R.id.chatMessage);
			holder.subtitle = (TextView) convertView
					.findViewById(R.id.chatSubtitle);
			holder.icon = (ImageView) convertView.findViewById(R.id.userIcon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.message
				.setText(conversationItemArray.get(position).getMessage());
		holder.subtitle.setText(conversationItemArray.get(position)
				.getSubtitle());
		holder.icon.setImageResource(conversationItemArray.get(position)
				.getIcon());

		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView message;
		TextView subtitle;
	}
}
