package ui.expense;

public class ExpenseModel {
    private String name;
    private String currency;
    private int foreignAmount;
    private int localAmount;

    public ExpenseModel(String name, String currency, int foreignAmount, int localAmount) {
        this.name = name;
        this.currency = currency;
        this.foreignAmount = foreignAmount;
        this.localAmount = localAmount;
    }

    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public int getForeignAmount() { return foreignAmount; }
    public int getLocalAmount() { return localAmount; }
}
