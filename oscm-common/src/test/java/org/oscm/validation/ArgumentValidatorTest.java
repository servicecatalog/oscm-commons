/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.validation;

import org.junit.Test;

import java.util.ArrayList;

public class ArgumentValidatorTest {

  @Test
  public void testConstructor() {
    new ArgumentValidator();
  }

  @Test
  public void testNotNullPositive() {
    ArgumentValidator.notNull("arg1", new Object());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotNullNegative() {
    ArgumentValidator.notNull("arg1", null);
  }

  @Test
  public void testNotNullNotEmptyPositive() {
    ArrayList<String> list = new ArrayList<String>();
    list.add("test_parameter");
    ArgumentValidator.notNullNotEmpty("arg1", list);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotNullNotEmptyNegative() {
    ArrayList<String> list = new ArrayList<String>();
    ArgumentValidator.notNullNotEmpty("arg1", list);
    ArgumentValidator.notNullNotEmpty("arg1", null);
  }

  @Test
  public void testNotEmptyStringPositive() {
    ArgumentValidator.notEmptyString("arg1", " test string with blanks   ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotEmptyStringNegative() {
    ArgumentValidator.notEmptyString("arg1", "          ");
  }
}
