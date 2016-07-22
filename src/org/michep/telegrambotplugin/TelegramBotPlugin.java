package org.michep.telegrambotplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardHide;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.Constants;
import com.bmc.arsys.api.StatusInfo;
import com.bmc.arsys.api.Value;
import com.bmc.arsys.pluginsvr.plugins.ARFilterAPIPluggable;
import com.bmc.arsys.pluginsvr.plugins.ARPluginContext;

public class TelegramBotPlugin implements ARFilterAPIPluggable {
	private Config config;
	private TelegramBot bot;

	@Override
	public void initialize(ARPluginContext ctx) throws ARException {
		config = new Config(ctx);
		ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_INFO, "<<>> initialize");
		bot = TelegramBot.getInstance(ctx, config.getProperty("BOT_Name"), config.getProperty("BOT_TOKEN"), this);
	}

	public void onEvent(ARPluginContext ctx, int arg1) throws ARException {

	}

	public void terminate(ARPluginContext ctx) throws ARException {

	}

	public List<Value> filterAPICall(ARPluginContext ctx, List<Value> args) throws ARException {
		String command = args.get(0).toString();
		String chatId = args.get(1).toString();
		String text = args.get(2).toString();
		switch (command) {
			case "SEND_MESSAGE":
				return sendMessage(ctx, chatId, text);
			case "SEND_CONTACT_REQUEST":
				String buttons = args.get(3).toString();
				return sendContactRequest(ctx, chatId, text, buttons);
			default:
				return null;
		}
	}

	private List<Value> sendContactRequest(ARPluginContext ctx, String chatId, String text, String button) throws ARException {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText(text);
			ReplyKeyboardMarkup kbMarkup = new ReplyKeyboardMarkup();
			KeyboardButton kb = new KeyboardButton();
			kb.setText(button);
			kb.setRequestContact(true);
			KeyboardRow kbRow = new KeyboardRow();
			kbRow.add(kb);
			List<KeyboardRow> kbRowList = new ArrayList<KeyboardRow>();
			kbRowList.add(kbRow);
			kbMarkup.setKeyboard(kbRowList);
			kbMarkup.setOneTimeKeyboad(true);
			kbMarkup.setResizeKeyboard(true);
			message.setReplayMarkup(kbMarkup);
			bot.sendMessage(message);
		} catch (TelegramApiException e) {
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
			StatusInfo statusInfo = new StatusInfo(Constants.AR_RETURN_ERROR, 10000, e.getMessage(), e.getApiResponse());
			List<StatusInfo> statusList = new ArrayList<StatusInfo>();
			statusList.add(statusInfo);
			ARException are = new ARException(statusList);
			throw are;
		}
		List<Value> valueList = new ArrayList<Value>();
		valueList.add(new Value("OK"));
		return valueList;
	}

	private List<Value> sendMessage(ARPluginContext ctx, String chatId, String text) throws ARException {
		try {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText(text);
			ReplyKeyboardHide kbHide = new ReplyKeyboardHide();
			kbHide.setHideKeyboard(true);
			message.setReplayMarkup(kbHide);
			bot.sendMessage(message);
		} catch (TelegramApiException e) {
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
			StatusInfo statusInfo = new StatusInfo(Constants.AR_RETURN_ERROR, 10000, e.getMessage(), e.getApiResponse());
			List<StatusInfo> statusList = new ArrayList<StatusInfo>();
			statusList.add(statusInfo);
			ARException are = new ARException(statusList);
			throw are;
		}
		List<Value> valueList = new ArrayList<Value>();
		valueList.add(new Value("OK"));
		return valueList;
	}

	public void createTransaction(Map<String, String> values) {

		try {
			config.createTransaction(values);
		} catch (ARException e) {
			ARPluginContext ctx = config.getPluginContext();
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
		}
	}
}