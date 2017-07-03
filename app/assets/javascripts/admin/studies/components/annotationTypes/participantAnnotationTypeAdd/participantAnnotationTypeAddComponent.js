/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _       = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/annotationTypes/participantAnnotationTypeAdd/participantAnnotationTypeAdd.html',
    controller: ParticipantAnnotationTypeAddController,
    controllerAs: 'vm',
    bindings: {
      study: '='
    }
  };

  var returnState = 'home.admin.studies.study.participants';

  ParticipantAnnotationTypeAddController.$inject = ['annotationTypeAddMixin'];

  /*
   * Controller for this component.
   */
  function ParticipantAnnotationTypeAddController(annotationTypeAddMixin) {
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

  return component;
});
