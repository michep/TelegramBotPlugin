package org.michep.telegrambotplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.michep.telegrambotplugin.ars.ARAdapter;
import org.michep.telegrambotplugin.ars.AREntry;
import org.michep.telegrambotplugin.ars.ARForm;

import com.bmc.arsys.api.ARException;

import com.bmc.arsys.pluginsvr.plugins.ARPluginContext;

public class Config {
	private Properties props;
	private ARPluginContext ctx;
	private ARForm formConfig;
	private ARForm formTransaction;
	// private ARForm formSessions;

	public Config(ARPluginContext ctx)  throws ARException {
		this.ctx = ctx;
		props = new Properties();
		formConfig = createConfigForm();
		loadConfig();
		formTransaction = createTransactionForm();
	}

	public String getProperty(String name) {
		return props.getProperty(name, "");
	}

	public ARPluginContext getPluginContext() {
		return ctx;
	}

	private ARForm createConfigForm() throws ARException {
		HashMap<Integer, String> fields = new HashMap<Integer, String>();
		fields.put(7, "Status");
		fields.put(8, "ParameterName");
		fields.put(1000000000, "ParameterValue");
		return new ARForm(ARAdapter.getInstance(ctx), "TBOT:Config", fields);

	}

	private ARForm createTransactionForm() throws ARException  {
		HashMap<Integer, String> fields = new HashMap<Integer, String>();
		fields.put(7, "Status");
		fields.put(536870916, "Type");
		fields.put(536870913, "ChatID");
		fields.put(536870914, "UserID");		
		fields.put(536870915, "Text");
		fields.put(536870919, "Timestamp");
		fields.put(536870930, "ContactPhoneNumber");
		fields.put(536870931, "MessageID");
		return new ARForm(ARAdapter.getInstance(ctx), "TBOT:Transaction", fields);
	}
	
	private void loadConfig() {
		try {
			List<AREntry> arentries = formConfig.getEntries();
			for (AREntry arentry : arentries) {
				props.put(arentry.getFieldStringValue("ParameterName"), arentry.getFieldStringValue("ParameterValue"));
			}
		} catch (ARException e) {
			ctx.logMessage(ARPluginContext.PLUGIN_LOG_LEVEL_ERROR, e.toString());
		}
	}

	public String createTransaction(Map<String, String> values) throws ARException {
		AREntry are = formTransaction.getNewEntry();
		are.setFieldValues(values);
		are.buildEntry();
		return formTransaction.createEntry(are);
	}
}