package org.michep.telegrambotplugin;

import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.bmc.arsys.pluginsvr.plugins.ARPluginContext;

public class TelegramBot extends TelegramLongPollingBot {

	private String botName;
	private String botToken;
	private ARPluginContext ctx;
	private static TelegramBot botInstance;
	private TelegramBotPlugin plugin;

	private TelegramBot(ARPluginContext ctx, String botName, String botToken, TelegramBotPlugin plugin) {
		this.ctx = ctx;
		this.botName = botName;
		this.botToken = botToken;
		this.plugin = plugin;
	}

	public void onUpdateReceived(Update update) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Status", "New");
		values.put("Type", "Incoming");
		values.put("MessageID", update.getMessage().getMessageId().toString());
		values.put("Text", update.getMessage().getText());
		values.put("ChatID", update.getMessage().getChatId().toString());
		values.put("UserID", update.getMessage().getFrom().getId().toString());
		values.put("Timestamp", update.getMessage().getDate().toString());
		if (update.getMessage().getContact() != null)
			values.put("ContactPhoneNumber", update.getMessage().getContact().getPhoneNumber());
		if (update.getMessage().hasDocument() == true)
			values.put("FileID", update.getMessage().getDocument().getFileId());
		plugin.createTransaction(values);
	}

	public String getBotToken() {
		return botToken;
	}

	public String getBotUsername() {
		return botName;
	}

	public static synchronized TelegramBot getInstance(ARPluginContext ctx, String botName, String botToken, TelegramBotPlugin plugin) {
		if (botInstance == null) {
			botInstance = new TelegramBot(ctx, botName, botToken, plugin);
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
			try {
				telegramBotsApi.registerBot(botInstance);
			} catch (TelegramApiException e) {
				ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_FATAL, e.toString());
			}
		}
		return botInstance;
	}
}