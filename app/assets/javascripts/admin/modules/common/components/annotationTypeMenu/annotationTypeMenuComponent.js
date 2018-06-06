/**
 * AngularJS Component used {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.annotationTypeMenu
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
class AnnotationTypeMenuController {

  constructor() {
    'ngInject';
    Object.assign(this, {});
  }

  $onInit() {
  }

  view() {
    if (this.onView()) {
      this.onView()(this.annotationType)
    }
  }

  update() {
    if (this.onUpdate()) {
      this.onUpdate()(this.annotationType)
    }
  }

  remove() {
    if (this.onRemove()) {
      this.onRemove()(this.annotationType)
    }
  }
}

/**
 * An AngularJS component that displays a context menu for {@link domain.annotations.AnnotationType
 * AnnotationTypes}.
 *
 * @memberOf admin.studies.components.annotationTypeMenu
 */
const annotationTypeMenuComponent = {
  template: require('./annotationTypeMenu.html'),
  controller: AnnotationTypeMenuController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<',
    allowChanges:   '<',
    onView:         '&',
    onUpdate:       '&',
    onRemove:       '&'
  }
};

export default ngModule => ngModule.component('annotationTypeMenu', annotationTypeMenuComponent);
