/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
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
      template: [
        '<annotation-type-view',
        '  study="vm.study"',
        '  annotation-type="vm.annotationType"',
        '  return-state="home.admin.studies.study.participants"',
        '  on-update="vm.onUpdate">',
        '</annotation-type-view>'
      ].join(''),
      controller: ParticipantAnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  ParticipantAnnotationTypeViewCtrl.$inject = [
    'notificationsService'
  ];

  function ParticipantAnnotationTypeViewCtrl(notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      vm.study.updateAnnotationType(annotationType)
        .then(postUpdate('Annotation type changed successfully.',
                         'Change successful',
                         1500))
        .catch(notificationsService.updateError);
    }

    function postUpdate(message, title, timeout) {
      return function (study) {
        vm.study = study;
        vm.annotationType = _.findWhere(vm.study.annotationTypes,
                                        { uniqueId: vm.annotationType.uniqueId });
        if (_.isUndefined(vm.annotationType)) {
          throw new Error('could not update annotation type');
        }
        notificationsService.success(message, title, timeout);
      };
    }

  }

  return participantAnnotationTypeViewDirective;

});
