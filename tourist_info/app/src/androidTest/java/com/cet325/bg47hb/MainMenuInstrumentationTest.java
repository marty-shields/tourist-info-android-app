package com.cet325.bg47hb;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class MainMenuInstrumentationTest extends ActivityInstrumentationTestCase2<MainMenu>{

    private Activity mMainMenu;
    private TextView mTextView1, mTextView2, mTextview3;

    public MainMenuInstrumentationTest(){
        super(MainMenu.class);
    }

    //set up
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        //starts activity with default intent
        mMainMenu = getActivity();
        mTextView1 = (TextView) mMainMenu.findViewById(R.id.textView_MenuSettings);
        mTextView2 = (TextView) mMainMenu.findViewById(R.id.textView_MenuBudget);
        mTextview3 = (TextView) mMainMenu.findViewById(R.id.textView_MenuPlaces);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    //after set up make sure text fixture has been set up correctly and does not crash
    public void testPreconditions(){
        //assert they are not null add message to say what is null
        assertNotNull("activity is null", mMainMenu);
        assertNotNull("edittext is null", mTextView1);
        assertNotNull("edittext is null", mTextView2);
        assertNotNull("edittext is null", mTextview3);
    }

    public void testPersistentData(){
        mTextView1 = (TextView) mMainMenu.findViewById(R.id.textView_MenuSettings);
        mTextView2 = (TextView) mMainMenu.findViewById(R.id.textView_MenuBudget);
        mTextview3 = (TextView) mMainMenu.findViewById(R.id.textView_MenuPlaces);
        final String expected = "999";

        mMainMenu.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView1.setText(expected);
                mTextView2.setText(expected);
                mTextview3.setText(expected);
            }
        });

        // Close the activity and see if the text we sent to mEditText persists
        mMainMenu.finish();
        setActivity(null);

        mMainMenu = getActivity();

        String actual1 = mTextView1.getText().toString();
        String actual2 = mTextView2.getText().toString();
        String actual3 = mTextview3.getText().toString();

        //assert they are the same
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
        assertEquals(expected, actual3);
    }

}
