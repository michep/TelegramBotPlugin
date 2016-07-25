package org.michep.telegrambotplugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardHide;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.AttachmentValue;
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
		switch (command) {
			case "SEND_MESSAGE":
				return sendMessage(ctx, args.get(1).toString(), args.get(2).toString());
			case "SEND_CONTACT_REQUEST":
				return sendContactRequest(ctx, args.get(1).toString(), args.get(2).toString(), args.get(3).toString());
			case "GET_FILE":
				return getFile(ctx, args.get(1).toString());
			default:
				return null;
		}
	}

	private List<Value> sendContactRequest(ARPluginContext ctx, String chatId, String text, String button) throws ARException {
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
		sendMessage(ctx, message);
		List<Value> valueList = new ArrayList<Value>();
		valueList.add(new Value("OK"));
		return valueList;
	}

	private List<Value> sendMessage(ARPluginContext ctx, String chatId, String text) throws ARException {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText(text);
		ReplyKeyboardHide kbHide = new ReplyKeyboardHide();
		kbHide.setHideKeyboard(true);
		message.setReplayMarkup(kbHide);
		sendMessage(ctx, message);
		List<Value> valueList = new ArrayList<Value>();
		valueList.add(new Value("OK"));
		return valueList;
	}

	private void sendMessage(ARPluginContext ctx, SendMessage message) throws ARException {
		try {
			bot.sendMessage(message);
		} catch (TelegramApiException e) {
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
			StatusInfo statusInfo = new StatusInfo(Constants.AR_RETURN_ERROR, 10000, e.getMessage(), e.getApiResponse());
			List<StatusInfo> statusList = new ArrayList<StatusInfo>();
			statusList.add(statusInfo);
			ARException are = new ARException(statusList);
			throw are;
		}
	}

	private List<Value> getFile(ARPluginContext ctx, String fileId) throws ARException {
		GetFile getFile = new GetFile();
		getFile.setFileId(fileId);
		InputStream is = null;
		try {
			File file = bot.getFile(getFile);
			String filePath = file.getFilePath();
			URL fileURL = new URL("https://api.telegram.org/file/bot" + bot.getBotToken() + "/" + filePath);
			byte[] buffer = new byte[file.getFileSize()];
			is = fileURL.openStream();
			is.read(buffer, 0, file.getFileSize());
			List<Value> valueList = new ArrayList<Value>();
			valueList.add(new Value("OK"));
			valueList.add(new Value(new AttachmentValue(buffer)));
			return valueList;
		} catch (TelegramApiException | IOException e) {
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
			StatusInfo statusInfo;
			if (e instanceof TelegramApiException) {
				TelegramApiException te = (TelegramApiException) e;
				statusInfo = new StatusInfo(Constants.AR_RETURN_ERROR, 10000, te.getMessage(), te.getApiResponse());
			} else
				statusInfo = new StatusInfo(Constants.AR_RETURN_ERROR, 10000, e.getMessage());
			List<StatusInfo> statusList = new ArrayList<StatusInfo>();
			statusList.add(statusInfo);
			ARException are = new ARException(statusList);
			throw are;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
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