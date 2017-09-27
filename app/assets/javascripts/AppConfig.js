/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

export default class AppConfigProvider {

    // initial / default config
  constructor() {
    this.config = {
      restApiUrlPrefix: '/api',
      dateFormat:       'YYYY-MM-DD',
      dateTimeFormat:   'YYYY-MM-DD HH:mm',
      datepickerFormat: 'yyyy-MM-dd HH:mm'
    };
  }

  set(settings) {
    this.config = settings;
  }

  $get() {
    return this.config;
  }

}
