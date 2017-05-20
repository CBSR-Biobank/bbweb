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
    '$q',
    'gettextCatalog',
    'notificationsService'
  ];

  function ParticipantAnnotationTypeViewCtrl($q,
                                             gettextCatalog,
                                             notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      return vm.study.updateAnnotationType(annotationType)
        .then(postUpdate)
        .then(notifySuccess)
        .catch(notificationsService.updateError);
    }

    function postUpdate(study) {
      vm.study = study;
      vm.annotationType = _.find(vm.study.annotationTypes, { id: vm.annotationType.id });
      if (_.isUndefined(vm.annotationType)) {
        return $q.reject('could not update annotation type');
      }
      return $q.when(true);
    }

    function notifySuccess() {
      return notificationsService.success(
        gettextCatalog.getString('Annotation type changed successfully.'),
        gettextCatalog.getString('Change successful'),
        1500);
    }

  }

  return participantAnnotationTypeViewDirective;

});
