/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function participantAnnotationTypeAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      template : [
        '<annotation-type-add',
        '  on-submit="vm.onSubmit"',
        '  on-cancel="vm.onCancel()"',
        '</annotation-type-add>',
      ].join(''),
      controller: ParticipantAnnotationTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  ParticipantAnnotationTypeAddCtrl.$inject = [
    '$state',
    'notificationsService',
    'domainEntityService'
  ];

  function ParticipantAnnotationTypeAddCtrl($state,
                                            notificationsService,
                                            domainEntityService) {
    var vm = this;

    vm.onSubmit = onSubmit;
    vm.onCancel = onCancel;

    //--

    function onSubmit(annotationType) {
      vm.study.addAnnotationType(annotationType).then(addSuccessful).catch(addFailed);

      function addSuccessful() {
        notificationsService.submitSuccess();
        $state.go('home.admin.studies.study.participants', {}, { reload: true });
      }

      function addFailed(error) {
        return domainEntityService.updateErrorModal(error, 'study');
      }
    }

    function onCancel() {
      $state.go('home.admin.studies.study.participants');
    }
  }

  return participantAnnotationTypeAddDirective;

});
