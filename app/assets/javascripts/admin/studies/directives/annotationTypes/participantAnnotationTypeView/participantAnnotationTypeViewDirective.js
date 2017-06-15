/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /*
   * Displays a single participant annotation type to the user.
   *
   * The user is allowed to change any of the attributes of the annotation type.
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
    'notificationsService',
    'breadcrumbService'
  ];

  function ParticipantAnnotationTypeViewCtrl($q,
                                             gettextCatalog,
                                             notificationsService,
                                             breadcrumbService) {
    var vm = this;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.participants',
        function () { return vm.study.name; }),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.participant.annotationTypeView',
        function () {
          if (_.isUndefined(vm.annotationType)) {
            return gettextCatalog.getString('Error');
          }
          return gettextCatalog.getString(
            'Participant annotation: {{name}}', { name: vm.annotationType.name });
        })
    ];

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
