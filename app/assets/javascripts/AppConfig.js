/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  // AppConfigProvider.$inject = [];

  function AppConfigProvider() {

    // initial / default config
    var config = {
      dateFormat:       'YYYY-MM-DD',
      dateTimeFormat:   'YYYY-MM-DD HH:mm',
      datepickerFormat: 'yyyy-MM-dd HH:mm',


    };

    return {
      set: function (settings) {
        config = settings;
      },
      $get: function () {
        return config;
      }
    };
  }

  return AppConfigProvider;
});
