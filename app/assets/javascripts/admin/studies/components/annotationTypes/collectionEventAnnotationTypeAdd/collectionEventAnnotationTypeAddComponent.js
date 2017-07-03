/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
    controller: CollectionEventAnnotationTypeAddController,
    controllerAs: 'vm',
    bindings: {
      study:               '=',
      collectionEventType: '='
    }
  };

  var returnState = 'home.admin.studies.study.collection.ceventType';

  CollectionEventAnnotationTypeAddController.$inject = ['annotationTypeAddMixin'];

  /*
   * Controller for this component.
   */
  function CollectionEventAnnotationTypeAddController(annotationTypeAddMixin) {
    var vm = this;

    _.extend(vm, annotationTypeAddMixin);

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function submit(annotationType) {
      vm.collectionEventType.addAnnotationType(annotationType)
        .then(vm.onAddSuccessful(returnState))
        .catch(vm.onAddFailed);
    }

    function cancel() {
      vm.onCancel(returnState)();
    }
  }

  return component;
});
