/** Study service */
define(['angular'], function(angular) {
  'use strict';

  StudyViewSettingsService.$inject = ['$log'];

  /**
   * Tracks wether each panel in the study view page is expanded or collapsed.
   *
   * TODO: save settings in local storage to remember them accross sessions.
   */
  function StudyViewSettingsService($log) {
    var currentState = initialSettings();
    var service = {
      initialSettings:  initialSettings,
      panelState:       panelState,
      panelStateToggle: panelStateToggle,
      initialize:       initialize,
      getState:         getState
    };

    return service;

    //-------

    function panelState(panel, state) {
      if (typeof currentState.panelStates[panel] === 'undefined') {
        throw new Error('panel not defined: ' + panel);
      }

      if (state === undefined) {
        return currentState.panelStates[panel];
      }
      currentState.panelStates[panel] = state;
      return state;
    }

    function panelStateToggle(panel) {
      if (typeof currentState.panelStates[panel] === 'undefined') {
        throw new Error('panel not defined: ' + panel);
      }
      currentState.panelStates[panel] = !currentState.panelStates[panel];
      $log.debug('panelStateToggle: ', panel, currentState.panelStates[panel]);
      return currentState.panelStates[panel];
    }

    function initialize(studyId) {
      if (studyId !== currentState.studyId) {
        // initialize state only when a new study is selected
        currentState = initialSettings();
        currentState.studyId = studyId;
      }
    }

    function getState() {
      return currentState;
    }

    function initialSettings() {
      var settings = {
        studyId: null,
        panelStates: {
          participantAnnotTypes: true,
          specimenGroups:        true,
          ceventAnnotTypes:      true,
          ceventTypes:           true,
          processingTypes:       true,
          spcLinkAnnotTypes:     true,
          spcLinkTypes:          true
        }
      };

      return settings;
    }
  }

  return StudyViewSettingsService;
});
