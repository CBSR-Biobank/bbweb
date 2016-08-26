/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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
      templateUrl : '/assets/javascripts/admin/studies/directives/annotationTypes/participantAnnotationTypeAdd/participantAnnotationTypeAdd.html',
      controller: ParticipantAnnotationTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  ParticipantAnnotationTypeAddCtrl.$inject = [
    'annotationTypeAddMixin'
  ];

  var returnState = 'home.admin.studies.study.participants';

  function ParticipantAnnotationTypeAddCtrl(annotationTypeAddMixin) {
    var vm = this;

    _.extend(vm, annotationTypeAddMixin);

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function submit(annotationType) {
      vm.study.addAnnotationType(annotationType)
        .then(vm.onAddSuccessful(returnState))
        .catch(vm.onAddFailed);
    }

    function cancel() {
      vm.onCancel(returnState)();
    }
  }

  return participantAnnotationTypeAddDirective;

});
