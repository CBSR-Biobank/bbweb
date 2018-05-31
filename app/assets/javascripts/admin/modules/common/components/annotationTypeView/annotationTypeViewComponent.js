/**
 * AngularJS Components used in Administration modules
 *
 * @namespace admin.common.components.annotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class AnnotationTypeViewController {

  constructor($state,
              gettextCatalog,
              modalInput,
              annotationTypeUpdateModal,
              annotationValueTypeLabelService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    modalInput,
                    annotationTypeUpdateModal,
                    annotationValueTypeLabelService
                  });
  }

  $onInit() {
    this.annotationTypeValueTypeLabel =
      this.annotationValueTypeLabelService.valueTypeToLabelFunc(this.annotationType.valueType,
                                                                this.annotationType.isSingleSelect());
  }

  editName() {
    this.modalInput.text(this.gettextCatalog.getString('Update Annotation Name'),
                         this.gettextCatalog.getString('Name'),
                         this.annotationType.name,
                         { required: true, minLength: 2 }).result
      .then(function (name) {
        this.annotationType.name = name;
        this.onUpdate()('name', this.annotationType);
      });
  }

  editDescription() {
    this.modalInput.textArea(this.gettextCatalog.getString('Update Annotation Description'),
                             this.gettextCatalog.getString('Description'),
                             this.annotationType.description)
      .result.then(function (description) {
        var annotationType = Object.assign({}, this.annotationType, { description: description });
        this.onUpdate()('description', annotationType);
      });
  }

  editRequired() {
    this.modalInput.boolean(this.gettextCatalog.getString('Update Annotation Required'),
                            this.gettextCatalog.getString('Required'),
                            this.annotationType.required,
                            { required: true }).result
      .then(function (required) {
        this.annotationType.required = required;
        this.onUpdate()('required', this.annotationType);
      });
  }

  editSelectionOptions() {
    this.annotationTypeUpdateModal.openModal(this.annotationType).result.then(function (options) {
      var annotationType = Object.assign({}, this.annotationType, { options: options });
      this.onUpdate()(annotationType);
    });
  }

  removeRequest() {
    if (this.onRemove()) {
      this.onRemove()(this.annotationType);
    }
  }

  back() {
    this.$state.go('^', {}, { reload: true });
  }

}

/**
 * An AngularJS component that allows the user to view a {@link domain.annotations.AnnotationType}.
 *
 * @memberOf admin.common.components.annotationTypeView
 *
 * @param {domain.annotations.AnnotationType} annotationType the Annotation type to display.
 *
 * @param {boolean} readOnly When *FALSE*, user is allowed to make changes to the annotation type.
 *
 * @param {admin.common.components.annotationTypeView.onUpdate} [onUpdate] Used only if `readOnly` is
 * *False*. This function called after the user made a change to the annotation type.
 */
const annotationTypeViewComponent = {
  template: require('./annotationTypeView.html'),
  controller: AnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<',
    readOnly:       '<',
    onUpdate:       '&',
    onRemove:       '&'
  }
};

/**
 * The callback function called by {@link
 * ng.admin.common.components.annotationTypeView.annotationTypeViewComponent annotationTypeViewComponent}
 * after the user has made changes to the annotation type.
 *
 * @callback admin.common.components.annotationTypeView.onUpdate
 * @param {string} attribute - the attribute that was modified
 * @param {domain.annotations.AnnotationType} annotationType - the updated annotation type.
 */

export default ngModule => ngModule.component('annotationTypeView', annotationTypeViewComponent)
