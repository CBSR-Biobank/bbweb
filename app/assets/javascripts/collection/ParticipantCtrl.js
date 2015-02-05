define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('ParticipantCtrl', ParticipantCtrl);

  ParticipantCtrl.$inject = [
    'AnnotationHelper',
    'study',
    'participant',
    'annotationTypes'
  ];

  /**
   *
   */
  function ParticipantCtrl(AnnotationHelper, study, participant, annotationTypes) {
    var vm = this;

    vm.study = study;
    vm.participant = participant;
    vm.annotationHelpers = getAnnotationHelpers(annotationTypes);

    setAnnotationValues();

    function getAnnotationHelpers(annotationTypes) {
      return _.map(annotationTypes, function(annotType) {
        return new AnnotationHelper(annotType);
      });
    }

    function setAnnotationValues() {
      _.each(vm.participant.annotations, function(annotation) {
        var ah = _.find(vm.annotationHelpers, function (ah) {
          return ah.annotationType.id === annotation.annotationTypeId;
        });
        if (ah) {
          ah.setValue(annotation);
        } else {
          throw new Error('matching annotation helper not found: ' + annotation.annotationTypeId);
        }
      });

    }

  }

});
