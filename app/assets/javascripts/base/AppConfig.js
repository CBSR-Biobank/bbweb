/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * This class encapsulates several constants used throughout the application.
 *
 * They can be changed at runtime if required.
 *
 * It is configured as an AngularJS provider.
 *
 * @memberOf base
 */
class AppConfigProvider {

  constructor() {

    /**
     * The default configuration.
     *
     * @property {string} restApiUrlPrefix - The prefix used by the server for it's REST API.
     * @property {string} dateFormat - The default format used to convert Date objects to strings.
     * @property {string} dateTimeFormat - The default format used to convert DateTime objects to strings.
     * @property {string} datepickerFormat - The default format used to the DatePicker component.
     */
    this.config = {
      restApiUrlPrefix: '/api',
      dateFormat:       'YYYY-MM-DD',
      dateTimeFormat:   'YYYY-MM-DD HH:mm',
      datepickerFormat: 'yyyy-MM-dd HH:mm'
    };

  }

  /**
   * Can be called by the application at run time to change one of these settings.
   *
   * @param {object} setttings - An object that contains the new settinsg.
   */
  set(settings) {
    this.config = settings;
  }

  /**
   * Allows for the config member object to be used as a getter.
   *
   * @return {object} An object that contains the current configuration..
   */
  $get() {
    return this.config;
  }

}

export default ngModule => ngModule.provider('AppConfig', AppConfigProvider)
