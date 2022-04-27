package jrm.core;

import jrm.constant.VarConstant;
import jrm.service.SendMessageOperationService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import jrm.store.HashMapStore;

import java.time.LocalDate;

public class CoreBot extends TelegramLongPollingBot {
    private SendMessageOperationService sendMessageOperationService = new SendMessageOperationService();
    private HashMapStore store = new HashMapStore();
    private boolean startPlanning;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            switch (update.getMessage().getText()) {
                case VarConstant.START:
                    executeMessage(sendMessageOperationService.createGreetingInformation(update));
                    executeMessage(sendMessageOperationService.createInstructionMessage(update));
                    break;
                case VarConstant.START_PLANNING:
                    startPlanning = true;
                    executeMessage(sendMessageOperationService.createPlanningMessage(update));
                    break;
                case VarConstant.END_PLANNING:
                    startPlanning = false;
                    executeMessage(sendMessageOperationService.createEndPlanningMessage(update));
                    break;
                case VarConstant.SHOW_DEALS:
                    if (startPlanning == false) {
                        executeMessage(sendMessageOperationService.createSimpleMessage(update, store.selectAll(LocalDate.now())));
                    }
                default:
                    if (startPlanning == true) {
                        store.save(LocalDate.now(), update.getMessage().getText());
                    }
            }
        }
        if (update.hasCallbackQuery()) {
            String instruction = "Бот для формирования дел на день. Чтобы воспользоваться ботом нажмите кнопку "+
                    "\"Начать планирование\" \nи следуйте инструкциям.";
            String callDate = update.getCallbackQuery().getData();
            if (VarConstant.YES.equals(callDate)) {
                EditMessageText text = sendMessageOperationService.createEditMessage(update, instruction);
                executeMessage(text);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "USERNAME";
    }

    @Override
    public String getBotToken() {
        return "TOKEN";
    }

    private <T extends BotApiMethod> void executeMessage(T sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
