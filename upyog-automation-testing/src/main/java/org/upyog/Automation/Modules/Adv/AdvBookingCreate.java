package org.upyog.Automation.Modules.Adv;

import java.io.File;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.upyog.Automation.Utils.ConfigReader;
import org.upyog.Automation.Utils.DriverFactory;


/**
 * Automated test class for UPYOG Advertisement Booking (Citizen)
 * Workflow:
 *  - Citizen login with OTP
 *  - Navigate to Advertisement → Book Advertisement
 *  - Fill advertisement details (dropdowns, dates, radio)
 *  - Select sites (checkboxes) and add to cart
 *  - View cart and book now
 *  - Fill applicant details & address
 *  - Upload documents (3 document rows on one page)
 *  - Submit application
 */
//@Component
public class AdvBookingCreate {

    /**
     * Main test method for Advertisement booking workflow.
     * Uncomment @PostConstruct above to run automatically on context init.
     */
    //@PostConstruct
    public void AdvBookingReg() {
        AdvBookingReg(ConfigReader.get("citizen.base.url"),
                     "Advertisement",
                     ConfigReader.get("citizen.mobile.number"),
                     ConfigReader.get("test.otp"),
                     ConfigReader.get("test.city.name"));
    }

    public void AdvBookingReg(String baseUrl, String moduleName, String mobileNumber, String otp, String cityName) {
        System.out.println("Advertisement Booking by Citizen");

        WebDriver driver = DriverFactory.createChromeDriver();
        WebDriverWait wait = DriverFactory.createWebDriverWait(driver);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        try {
            // STEP 1: Citizen Login
            performCitizenLogin(driver, wait, js, actions, baseUrl, mobileNumber, otp, cityName);

            // STEP 2: Navigate to Advertisement Booking
            navigateToAdvertisement(driver, wait, js);

            // STEP 3: Fill Advertisement Details and Search
            fillAdvertisementDetails(driver, wait, js);

            // STEP 4: Select checkboxes and add to cart
            selectCheckboxesAndAddToCart(driver, wait, js);

            // STEP 5: View cart and Book Now
            viewCartAndBookNow(driver, wait, js);

            // STEP 6: Address Details (always Fill New Details for automation)
            handleAddressDetails(driver, wait, js);

            // STEP 7: Applicant Details
            fillApplicantDetails(driver, wait, js);

            // STEP 8: Applicant Address Details
            fillApplicantAddressDetails(driver, wait, js);

            // STEP 9: Upload Documents (3 rows on one page)
            uploadDocuments(driver, wait, js);

            // STEP 10: Submit Application
            submitApplication(driver, wait, js);

            System.out.println("Advertisement Booking completed successfully!");
            Thread.sleep(50000); // Keep browser open for observation

        } catch (Exception e) {
            System.out.println("Exception in Advertisement Booking: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // driver.quit();
        }
    }

    // =====================================================================
    // STEP 1: CITIZEN LOGIN (same style as TradeLicenseCreate)
    // =====================================================================

    private void performCitizenLogin(WebDriver driver, WebDriverWait wait, JavascriptExecutor js, Actions actions, String baseUrl, String mobileNumber, String otp, String cityName)
            throws InterruptedException {

        driver.get(baseUrl);
        System.out.println("Open the Citizen Login Portal");

        // Mobile number
        fillInput(wait, "mobileNumber", mobileNumber);

        // Accept terms checkbox
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[type='checkbox'].form-field")));
        if (!checkbox.isSelected()) {
            js.executeScript("arguments[0].click();", checkbox);
            Thread.sleep(1000);
        }

        // Next
        clickButton(wait, js, "//button[@type='submit']//header[text()='Next']/..");

        // OTP
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.input-otp-wrap")));
        List<WebElement> otpInputs = driver.findElements(By.cssSelector("input.input-otp"));
        for (int i = 0; i < otp.length() && i < otpInputs.size(); i++) {
            otpInputs.get(i).sendKeys(String.valueOf(otp.charAt(i)));
        }

        // Submit OTP
        clickButton(wait, js, "//button[@type='submit']//header[text()='Next']/..");

        // Select city`1
        selectCity(driver, wait, js, cityName);

        // Continue
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'submit-bar') and contains(., 'Continue')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", continueBtn);
        actions.moveToElement(continueBtn).click().perform();
    }

    // =====================================================================
    // STEP 2: NAVIGATE TO ADVERTISEMENT MODULE
    // =====================================================================

    private void navigateToAdvertisement(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Navigating to Advertisement Booking");

        // Sidebar Advertisement link
        js.executeScript("arguments[0].click();", wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[@href='/upyog-ui/citizen/ads-home']"))));

        Thread.sleep(2000);
        System.out.println("Reached Advertisement home page");

        // "Advertisement Book" link
        js.executeScript("arguments[0].click();", wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@href='/upyog-ui/citizen/ads/bookad']"))));

        System.out.println("Clicked Advertisement Book link");
    }

    // =====================================================================
    // STEP 3: ADVERTISEMENT DETAILS (dropdowns + dates + radio + search)
    // =====================================================================

    private void fillAdvertisementDetails(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Filling Advertisement Details");

        // Example: 3 dropdowns – Category, Ad Type, Location (adjust as per UI)
        selectDropdownOption(driver, wait, js, 0);
        selectDropdownOption(driver, wait, js, 1);
        selectDropdownOption(driver, wait, js, 2);

        // Date range (type="date" → yyyy-MM-dd)
        List<WebElement> dateInputs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("input.employee-card-input[type='date']")));

        if (dateInputs.size() >= 2) {
            WebElement fromDate = dateInputs.get(0);
            WebElement toDate = dateInputs.get(1);

            fromDate.clear();
            fromDate.sendKeys("21-12-2025");

            toDate.clear();
            toDate.sendKeys("25-01-2026");
        } else {
            System.out.println("Date inputs not found or less than 2");
        }

        // Radio: Advertisement With Night Light? -> No
        selectRadioButtonByLabel(driver, "No");

        // Search
        clickButtonByHeader(driver, wait, "Search");
        Thread.sleep(2000);
    }

    // =====================================================================
    // STEP 4: SELECT CHECKBOXES AND ADD TO CART
    // =====================================================================

    private void selectCheckboxesAndAddToCart(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Selecting checkboxes and adding to cart");

        // Only checkboxes inside result table
        By resultCheckboxesLocator = By.cssSelector("table tbody input[type='checkbox']");
        List<WebElement> checkboxes = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(resultCheckboxesLocator));

        System.out.println("Found " + checkboxes.size() + " result checkboxes");

        for (int i = 0; i < checkboxes.size(); i++) {
            try {
                WebElement checkbox = checkboxes.get(i);
                js.executeScript("arguments[0].scrollIntoView(true);", checkbox);
                Thread.sleep(200);
                if (!checkbox.isSelected()) {
                    js.executeScript("arguments[0].click();", checkbox);
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                System.out.println("Error clicking checkbox " + i + ": " + e.getMessage());
            }
        }

        // Add to Cart button
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Add to cart') or contains(., 'Add to Cart')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", addToCartBtn);
        Thread.sleep(300);
        js.executeScript("arguments[0].click();", addToCartBtn);
        System.out.println("Clicked Add to Cart");
        Thread.sleep(2000);
    }

    // =====================================================================
    // STEP 5: VIEW CART AND BOOK NOW
    // =====================================================================

    private void viewCartAndBookNow(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Viewing cart and booking");

        WebElement viewCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'View Cart')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", viewCartBtn);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", viewCartBtn);
        System.out.println("Clicked View Cart");

        Thread.sleep(2000);

        WebElement bookNowBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Book Now')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", bookNowBtn);
        Thread.sleep(500);
        js.executeScript("arguments[0].click();", bookNowBtn);
        System.out.println("Clicked Book Now");

        Thread.sleep(2000);
    }

    // =====================================================================
    // STEP 6: ADDRESS DETAILS – FILL NEW DETAILS
    // =====================================================================

    private void handleAddressDetails(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Handling address details – Fill New Details");

        // For automation stability we always choose "Fill New Details"
        WebElement fillNewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Fill New Details')]")));
        js.executeScript("arguments[0].scrollIntoView(true);", fillNewBtn);
        js.executeScript("arguments[0].click();", fillNewBtn);
        Thread.sleep(2000);

        clickNextButton(driver, wait, js);
    }

    // =====================================================================
    // STEP 7: APPLICANT DETAILS
    // =====================================================================

    private void fillApplicantDetails(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Filling Applicant Details");

        fillInput(wait, "applicantName", "Arpit Rao");
        fillInput(wait, "emailId", "arpit@gmail.com");

        Thread.sleep(500);
        clickNextButton(driver, wait, js);
    }

    // =====================================================================
    // STEP 8: APPLICANT ADDRESS DETAILS
    // =====================================================================

    private void fillApplicantAddressDetails(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Filling Applicant Address Details");

        fillInput(wait, "houseNo", "A-12");
        fillInput(wait, "houseName", "Jagbir Bhawan");
        fillInput(wait, "streetName", "qwerty");
        fillInput(wait, "addressline1", "qwerty123");
        fillInput(wait, "addressline2", "qwerty2");
        fillInput(wait, "landmark", "qwerty123");

        // City & Locality dropdowns on this page (index may differ – adjust if needed)
        selectDropdownOption(driver, wait,js, 0); // City
        Thread.sleep(2000);
        selectDropdownOption(driver, wait,js, 1); // Locality

        fillInput(wait, "pincode", "110011");

        Thread.sleep(500);
        clickNextButton(driver, wait, js);
    }

    // =====================================================================
    // STEP 9: UPLOAD DOCUMENTS (3 rows on a single page)
    // =====================================================================

    // STEP 9: Upload Documents (Advertisement)
    private void uploadDocuments(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Uploading Documents");

        // 1) Select all three dropdowns (any valid option – first one is fine for automation)
        // Advertisement Sample Document
        selectDropdownOption(driver, wait,js, 0);

        // Applicant Address Proof
        selectDropdownOption(driver, wait,js, 1);

        // Applicant Identity Proof
        selectDropdownOption(driver, wait,js, 2);

        // 2) Upload 3 files in order – using your existing config keys
        uploadFile(driver, wait, js, 0, ConfigReader.get("document.sampleDocument.proof"));   // top row
        uploadFile(driver, wait, js, 1, ConfigReader.get("document.address.proof"));      // middle row
        uploadFile(driver, wait, js, 2, ConfigReader.get("document.Identity.proof")); // bottom row

        Thread.sleep(1000);

        // 3) Click Next
        clickNextButton(driver, wait, js);
        System.out.println("Finished Upload Documents step");
    }


    // =====================================================================
    // STEP 10: SUBMIT APPLICATION
    // =====================================================================

    private void submitApplication(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Submitting Advertisement Application");

        Thread.sleep(5000);
        // Declaration checkbox – assume last checkbox on page
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (!checkboxes.isEmpty()) {
            WebElement lastCheckbox = checkboxes.get(checkboxes.size() - 1);
            try {
                if (!lastCheckbox.isSelected()) {
                    js.executeScript("arguments[0].scrollIntoView(true);", lastCheckbox);
                    Thread.sleep(300);
                    js.executeScript("arguments[0].click();", lastCheckbox);
                    System.out.println("Checked declaration checkbox");
                }
            } catch (Exception ex) {
                System.out.println("Could not click declaration checkbox: " + ex.getMessage());
            }
        }

        // Click Submit
        WebElement submitButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@class='submit-bar ' and @type='button'][.//header[text()='Submit']]")));
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(300);
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", submitButton);
        Thread.sleep(200);
        submitButton.click();
        System.out.println("Advertisement application: Submit clicked");

        // Wait for success acknowledgement (green banner) and then handle payment
        try {
            // Wait for a success-like message on the page (contains 'successfully' or 'Booking No')
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            "//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'successfully')]")),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            "//*[contains(., 'Booking No') or contains(.,'Booking No.')]"))
            ));

            System.out.println("Application submitted successfully (acknowledgement detected).");

            // Call payment handler (STEP 11)
            handlePaymentFlow(driver, wait, js);

        } catch (Exception e) {
            System.out.println("Post-submit: success acknowledgement NOT detected within timeout: " + e.getMessage());
            // still attempt payment step in case page navigated directly to bill
            try {
                handlePaymentFlow(driver, wait, js);
            } catch (Exception ex) {
                System.out.println("handlePaymentFlow also failed/skipped: " + ex.getMessage());
            }
        }

        System.out.println("Advertisement Booking completed successfully!");
        Thread.sleep(50000);
    }


    //======================================================================
    // STEP 11: MAKE PAYMENT
    // =====================================================================

    /**
     * Helper: wait for common overlays/loaders to disappear
     */
    private void waitForNoOverlay(WebDriver driver, WebDriverWait wait) {
        try {
            java.util.List<By> loaderSelectors = java.util.Arrays.asList(
                    By.cssSelector(".loading"),
                    By.cssSelector(".overlay"),
                    By.cssSelector(".loader"),
                    By.cssSelector(".submit-bar-disabled"),
                    By.cssSelector(".is-loading"),
                    By.cssSelector(".ant-modal-root .ant-spin")
            );
            for (By sel : loaderSelectors) {
                try {
                    wait.until(org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated(sel));
                } catch (Exception ignored) {
                    // not present / timed out -> continue
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Helper: try clicking an element multiple times, with a JS fallback.
     * Returns true if clicked.
     */
    private boolean tryClickWithRetries(WebDriver driver, WebDriverWait wait, JavascriptExecutor js, By locator,
                                        int timeoutSeconds, int retries, long retryDelayMs) throws InterruptedException {
        WebDriverWait localWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds));

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                waitForNoOverlay(driver, wait);
                WebElement el = localWait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(locator));
                try {
                    el.click();
                    System.out.println("Clicked element " + locator + " (attempt " + attempt + ")");
                    return true;
                } catch (Exception clickEx) {
                    // fallback to JS click
                    try {
                        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                        Thread.sleep(150);
                        js.executeScript("arguments[0].click();", el);
                        System.out.println("JS-clicked element " + locator + " (attempt " + attempt + ")");
                        return true;
                    } catch (Exception jsEx) {
                        System.out.println("Click failed attempt " + attempt + " for " + locator + " : " + jsEx.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("Element not clickable yet (" + locator + ") attempt " + attempt + " : " + e.getMessage());
            }
            Thread.sleep(retryDelayMs);
        }
        return false;
    }

    private WebElement waitForNetBankingTab(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        System.out.println("Waiting for Net Banking tab in left sidebar...");

        // 1) Wait for the left sidebar to render fully
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@class,'left') or contains(@class,'side') or contains(@class,'menu')]"))
            );
        } catch (Exception e) {
            System.out.println("Sidebar not detected yet: " + e.getMessage());
        }

        Thread.sleep(800);

        // 2) Scroll sidebar down — Net Banking is always below Cards
        js.executeScript("window.scrollBy(0, 400);");
        Thread.sleep(300);
        js.executeScript("window.scrollBy(0, 400);");
        Thread.sleep(300);

        // 3) Now find Net Banking using multiple possible matches
        By[] possibleLocators = new By[]{
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'net banking')]"),
                By.xpath("//div[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'net banking')]"),
                By.xpath("//li[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'net banking')]"),
                By.xpath("//span[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'net banking')]"),
                By.xpath("//*[contains(@href,'ibank')]")  // SurePay direct link
        };

        for (By locator : possibleLocators) {
            try {
                WebElement ele = wait.until(ExpectedConditions.elementToBeClickable(locator));
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", ele);
                Thread.sleep(200);
                System.out.println("Found Net Banking element using: " + locator);
                return ele;
            } catch (Exception ignore) {}
        }

        System.out.println("Net Banking tab NOT found in sidebar.");
        return null;
    }


    /**
     * Payment flow tailored to: Net Banking -> select ICICI -> Pay -> click Success on mock bank
     * Improved with retries and overlay handling to avoid stuck at Proceed/Pay.
     */
    /**
     * Payment flow: Card → Pay Now → Razorpay mock "Success"
     */
    private void handlePaymentFlow(WebDriver driver, WebDriverWait wait, JavascriptExecutor js) throws InterruptedException {
        System.out.println("Starting payment flow (Card → Pay Now → Success)...");

        // remember UPYOG window
        String mainHandle = driver.getWindowHandle();

        // -----------------------------
        // STEP 1: "Make Payment" (ack page)
        // -----------------------------
        try {
            By makePaymentSel = By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'make payment')]");
            boolean clicked = tryClickWithRetries(driver, wait, js, makePaymentSel, 25, 4, 700);
            if (clicked) {
                System.out.println("Clicked 'Make Payment'");
                Thread.sleep(1000);
            } else {
                System.out.println("'Make Payment' button not found or not clickable, continuing...");
            }
        } catch (Exception e) {
            System.out.println("'Make Payment' error: " + e.getMessage());
        }

        // -----------------------------
        // STEP 2: "Proceed To Pay" (Tax Bill Details page)
        // -----------------------------
        try {
            By proceedSel = By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'proceed to pay') or " +
                            "(contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'proceed') and " +
                            " contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pay'))]");
            boolean proceedClicked = tryClickWithRetries(driver, wait, js, proceedSel, 40, 5, 800);
            if (proceedClicked) {
                System.out.println("Clicked 'Proceed To Pay'");
                Thread.sleep(1500);
            } else {
                System.out.println("'Proceed To Pay' not found or not clickable, continuing...");
            }
        } catch (Exception e) {
            System.out.println("'Proceed To Pay' error: " + e.getMessage());
        }

        // -----------------------------
        // STEP 3: "Pay" on UPYOG payment-method page (PAYGOV)
        // -----------------------------
        try {
            // usually PAYGOV is already selected, we just click Pay
            By payButtonSel = By.xpath(
                    "//button[contains(@class,'submit-bar') and " +
                            " ( .//header[normalize-space(.)='Pay'] " +
                            "   or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pay'))]");
            boolean payClicked = tryClickWithRetries(driver, wait, js, payButtonSel, 40, 5, 800);
            if (payClicked) {
                System.out.println("Clicked UPYOG 'Pay' button (PAYGOV)");
                Thread.sleep(2000); // allow redirect to SurePay
            } else {
                System.out.println("UPYOG 'Pay' button not found or not clickable.");
            }
        } catch (Exception e) {
            System.out.println("Error clicking UPYOG 'Pay' button: " + e.getMessage());
        }

        /* ------------------------------------------------------
       STEP 4 → CLICK "NET BANKING" TAB
    ------------------------------------------------------ */
        try {
            java.util.List<By> NETBANKING_LOCATORS = java.util.Arrays.asList(
                    By.xpath("//a[contains(.,'Net Banking')]"),
                    By.xpath("//div[contains(.,'Net Banking')]"),
                    By.xpath("//button[contains(.,'Net Banking')]"),
                    By.xpath("//*[contains(text(),'Net Banking')]")
            );

            WebElement netBankingTab = null;

            for (By sel : NETBANKING_LOCATORS) {
                try {
                    netBankingTab = wait.until(ExpectedConditions.elementToBeClickable(sel));
                    if (netBankingTab != null) break;
                } catch (Exception ignored) {}
            }

            if (netBankingTab != null) {
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", netBankingTab);
                Thread.sleep(200);
                js.executeScript("arguments[0].click();", netBankingTab);
                System.out.println("Clicked NET BANKING tab");
            } else {
                System.out.println("⚠ Net Banking tab NOT FOUND — maybe gateway UI changed or hidden.");
            }

            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("Error clicking Net Banking tab: " + e.getMessage());
        }

    /* ------------------------------------------------------
       STEP 5 → SELECT ICICI BANK
    ------------------------------------------------------ */
        try {
            Thread.sleep(800);

            java.util.List<WebElement> iciciOptions =
                    driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'icici')]"));

            if (iciciOptions.isEmpty()) {
                // Try bank icon alt text
                iciciOptions = driver.findElements(By.xpath("//img[contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'icici')]/parent::*"));
            }

            if (!iciciOptions.isEmpty()) {
                WebElement icici = iciciOptions.get(0);
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", icici);
                Thread.sleep(200);
                js.executeScript("arguments[0].click();", icici);
                System.out.println("Selected ICICI Bank");
            } else {
                System.out.println("⚠ ICICI not found — clicking first available bank option");

                java.util.List<WebElement> bankTiles =
                        driver.findElements(By.xpath("//div[contains(@class,'bank') or contains(@class,'tile')]"));

                if (!bankTiles.isEmpty()) {
                    WebElement first = bankTiles.get(0);
                    js.executeScript("arguments[0].scrollIntoView({block:'center'});", first);
                    Thread.sleep(200);
                    js.executeScript("arguments[0].click();", first);
                    System.out.println("Clicked fallback BANK tile.");
                }
            }

            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Error selecting bank: " + e.getMessage());
        }

    /* ------------------------------------------------------
       STEP 6 → CLICK "PAY" BUTTON
    ------------------------------------------------------ */
        try {
            By payBtn = By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pay') " +
                            "and not(contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cancel'))]"
            );

            boolean ok = tryClickWithRetries(driver, wait, js, payBtn, 30, 5, 600);

            if (ok) {
                System.out.println("Clicked PAY button");
            } else {
                System.out.println("⚠ Pay button NOT FOUND on gateway.");
            }

            Thread.sleep(1500);

        } catch (Exception e) {
            System.out.println("Error clicking Pay button: " + e.getMessage());
        }

    /* ------------------------------------------------------
       STEP 7 → CLICK "SUCCESS" (MOCK BANK PAGE)
    ------------------------------------------------------ */
        try {
            Thread.sleep(1500);

            WebElement successBtn = null;

            successBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")
            ));

            if (successBtn != null) {
                js.executeScript("arguments[0].scrollIntoView({block:'center'});", successBtn);
                Thread.sleep(2000);
                js.executeScript("arguments[0].click();", successBtn);
                System.out.println("Clicked SUCCESS on mock bank");
            }

        } catch (Exception e) {
            System.out.println("Success button not found: " + e.getMessage());
        }

        // -----------------------------
        // STEP 8: Switch back to UPYOG window
        // -----------------------------
        try {
            driver.switchTo().window(mainHandle);
        } catch (Exception e) {
            System.out.println("Could not switch back to UPYOG handle directly: " + e.getMessage());
            // fallback: pick any window that has 'upyog' in URL
            try {
                java.util.Set<String> handles = driver.getWindowHandles();
                for (String h : handles) {
                    driver.switchTo().window(h);
                    try {
                        String url = driver.getCurrentUrl();
                        if (url != null && url.toLowerCase().contains("upyog")) {
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        System.out.println("Payment flow finished (Card route).");
    }


    /** small helper to read current url safely */
    private String safeGetUrl(WebDriver driver) {
        try { return driver.getCurrentUrl(); } catch (Exception e) { return "unknown"; }
    }



    // =====================================================================
    // UTILITY METHODS
    // =====================================================================

    private void fillInput(WebDriverWait wait, String fieldName, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.name(fieldName)));
        input.clear();
        input.sendKeys(value);
    }

    // optional field – do not fail if missing
    private void fillOptionalInput(WebDriver driver, WebDriverWait wait, String fieldName, String value) {
        try {
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(fieldName)));
            if (input.isDisplayed() && input.isEnabled()) {
                input.clear();
                input.sendKeys(value);
                System.out.println("Filled optional field: " + fieldName);
            } else {
                System.out.println("Optional field " + fieldName + " not interactable, skipping");
            }
        } catch (Exception e) {
            System.out.println("Optional field " + fieldName + " not found, skipping");
        }
    }

    private void clickButton(WebDriverWait wait, JavascriptExecutor js, String xpath) throws InterruptedException {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        js.executeScript("arguments[0].scrollIntoView(true);", button);
        Thread.sleep(300);
        button.click();
    }

    private void clickButtonByHeader(WebDriver driver, WebDriverWait wait, String headerText)
            throws InterruptedException {

        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'submit-bar') and .//header[contains(text(),'" + headerText + "')]]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        Thread.sleep(500);
    }

    private void clickNextButton(WebDriver driver, WebDriverWait wait, JavascriptExecutor js)
            throws InterruptedException {

        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'submit-bar') and .//header[text()='Next']]")));
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", nextButton);
        Thread.sleep(200);
        nextButton.click();
        System.out.println("Clicked Next");
    }

    private void selectCity(WebDriver driver, WebDriverWait wait, JavascriptExecutor js, String cityName)
            throws InterruptedException {

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.radio-wrap.reverse-radio-selection-wrapper")));

        List<WebElement> cityOptions = driver.findElements(
                By.cssSelector("div.radio-wrap.reverse-radio-selection-wrapper div"));

        for (WebElement option : cityOptions) {
            WebElement label = option.findElement(By.tagName("label"));
            if (label.getText().trim().equals(cityName)) {
                WebElement radioInput = option.findElement(By.cssSelector("input[type='radio']"));
                if (!radioInput.isSelected()) {
                    js.executeScript("arguments[0].click();", radioInput);
                    Thread.sleep(1000);
                }
                return;
            }
        }
        throw new RuntimeException("Failed to select city: " + cityName);
    }

    private void selectRadioButtonByLabel(WebDriver driver, String labelText) {
        try {
            WebElement radio = null;

            try {
                radio = driver.findElement(By.xpath("//label[text()='" + labelText + "']/preceding-sibling::span/input"));
            } catch (Exception e1) {
                try {
                    radio = driver.findElement(By.xpath("//label[contains(text(),'" + labelText + "')]/preceding-sibling::input"));
                } catch (Exception e2) {
                    try {
                        radio = driver.findElement(By.xpath("//label[text()='" + labelText + "']/..//input[@type='radio']"));
                    } catch (Exception e3) {
                        try {
                            radio = driver.findElement(By.xpath("//label[text()='" + labelText + "']/following-sibling::input[@type='radio']"));
                        } catch (Exception e4) {
                            radio = driver.findElement(By.xpath("//input[@type='radio'][@value='" + labelText + "']"));
                        }
                    }
                }
            }

            if (radio != null && !radio.isSelected()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", radio);
                Thread.sleep(200);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
                System.out.println("Selected radio button: " + labelText);
            }
        } catch (Exception e) {
            System.out.println("Error selecting radio button '" + labelText + "': " + e.getMessage());
            throw new RuntimeException("Failed to select radio button: " + labelText, e);
        }
    }

    private void selectDropdownOption(WebDriver driver,
                                      WebDriverWait wait,
                                      JavascriptExecutor js,
                                      int dropdownIndex) throws InterruptedException {

        // get all dropdown arrow svgs on the page
        java.util.List<WebElement> dropdownSvgs = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector("div.select svg.cp"))
        );

        if (dropdownIndex < 0 || dropdownIndex >= dropdownSvgs.size()) {
            System.out.println("Dropdown index " + dropdownIndex + " not found. Total: " + dropdownSvgs.size());
            return;
        }

        WebElement svg = dropdownSvgs.get(dropdownIndex);

        // scroll into view
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", svg);
        Thread.sleep(200);

        // 1st try: normal Selenium click
        try {
            svg.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Normal click intercepted on dropdown svg, using JS dispatch. Reason: " + e.getMessage());

            // 2nd try: dispatch a click event manually (no arguments[0].click())
            js.executeScript(
                    "var ev = document.createEvent('MouseEvents');" +
                            "ev.initEvent('click', true, true);" +
                            "arguments[0].dispatchEvent(ev);",
                    svg
            );
        }

        // wait for options panel
        WebElement optionsContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.options-card"))
        );

        java.util.List<WebElement> options = optionsContainer.findElements(
                By.cssSelector("div.profile-dropdown--item")
        );

        if (!options.isEmpty()) {
            WebElement firstOption = options.get(0);
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", firstOption);
            Thread.sleep(150);

            try {
                firstOption.click();
            } catch (ElementClickInterceptedException e) {
                js.executeScript(
                        "var ev = document.createEvent('MouseEvents');" +
                                "ev.initEvent('click', true, true);" +
                                "arguments[0].dispatchEvent(ev);",
                        firstOption
                );
            }
        } else {
            System.out.println("No options found in dropdown for index " + dropdownIndex);
        }
    }

    /**
     * For document page: we already have the list of svg dropdown icons.
     */
    private void selectDropdownOptionByIndex(WebDriver driver,
                                             WebDriverWait wait,
                                             JavascriptExecutor js,
                                             List<WebElement> dropdownSvgs,
                                             int dropdownIndex,
                                             int optionIndex) throws InterruptedException {

        if (dropdownIndex >= dropdownSvgs.size()) {
            System.out.println("Dropdown index " + dropdownIndex + " out of range");
            return;
        }

        WebElement svg = dropdownSvgs.get(dropdownIndex);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", svg);
        Thread.sleep(200);

        js.executeScript("arguments[0].click();", svg);

        WebElement optionsContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.options-card"))
        );

        List<WebElement> options = optionsContainer.findElements(By.cssSelector("div.profile-dropdown--item"));

        if (!options.isEmpty() && optionIndex < options.size()) {
            js.executeScript("arguments[0].click();", options.get(optionIndex));
        }
    }


    private void uploadFileToInput(JavascriptExecutor js, List<WebElement> fileInputs, int index, String filePath)
            throws InterruptedException {

        if (index >= fileInputs.size()) {
            System.out.println("⚠ No file input at index " + index + " for " + filePath);
            return;
        }

        java.io.File f = new java.io.File(filePath);
        System.out.println("Attempting upload from: " + f.getAbsolutePath() + "  exists? " + f.exists());

        if (!f.exists()) {
            System.out.println("⚠ File does NOT exist on disk. Skipping this input.");
            return;
        }

        WebElement input = fileInputs.get(index);

        // Make sure Selenium can interact (visibility & scroll)
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", input);
        js.executeScript("arguments[0].style.opacity='1'; arguments[0].style.display='block';", input);
        Thread.sleep(300);

        input.sendKeys(f.getAbsolutePath());
        System.out.println("✅ Uploaded document into input index " + index);
    }


    private void uploadFile(WebDriver driver, WebDriverWait wait, JavascriptExecutor js,
                            int index, String filePath) throws InterruptedException {

        // All file inputs on the page – match your screenshot (hidden absolute-position input)
        List<WebElement> fileInputs = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("input[type='file'].input-mirror-selector-button"))
        );

        if (index >= fileInputs.size()) {
            System.out.println("File input index " + index + " not found for path: " + filePath);
            return;
        }

        WebElement fileInput = fileInputs.get(index);

        // Make sure Selenium can interact with the hidden input
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", fileInput);
        js.executeScript("arguments[0].style.opacity='1'; arguments[0].style.display='block';", fileInput);
        Thread.sleep(300);

        fileInput.sendKeys(getAbsolutePath(filePath));
        System.out.println("Uploaded file at index " + index + ": " + filePath);
        Thread.sleep(500);
    }

    /**
     * Converts relative path to absolute path for file uploads
     * Selenium requires absolute paths for sendKeys() on file inputs
     */
    private String getAbsolutePath(String relativePath) {
        File file = new File(relativePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + relativePath);
        }
        return file.getAbsolutePath();
    }
}