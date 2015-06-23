/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  ParticipantCtrl.$inject = [
    'Participant',
    'study',
    'participant',
    'annotationTypes'
  ];

  /**
   *
   */
  function ParticipantCtrl(Participant,
                           study,
                           participant,
                           annotationTypes) {
    var vm = this;
    vm.study = study;
    vm.participant = participant;
  }

  return ParticipantCtrl;
});
