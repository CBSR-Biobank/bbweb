/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function participantAnnotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:          '=',
        annotationType: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/annotationTypes/participantAnnotationTypeView/participantAnnotationTypeView.html',
      controller: ParticipantAnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  ParticipantAnnotationTypeViewCtrl.$inject = [
    '$q', 'notificationsService'
  ];

  function ParticipantAnnotationTypeViewCtrl($q, notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      return vm.study.updateAnnotationType(annotationType)
        .then(postUpdate)
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function postUpdate(study) {
      var deferred = $q.defer();

      vm.study = study;
      vm.annotationType = _.find(vm.study.annotationTypes,
                                      { uniqueId: vm.annotationType.uniqueId });
      if (_.isUndefined(vm.annotationType)) {
        deferred.reject('could not update annotation type');
      } else {
        deferred.resolve(true);
      }
      return deferred.promise;
    }

    function notifySuccess() {
      return notificationsService.success(
        'Annotation type changed successfully.',
        'Change successful',
        1500);
    }

  }

  return participantAnnotationTypeViewDirective;

});
