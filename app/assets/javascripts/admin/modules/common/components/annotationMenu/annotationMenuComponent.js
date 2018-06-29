/**
 * AngularJS Component used {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.annotationMenu
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
class AnnotationMenuController {

  constructor() {
    'ngInject';
    Object.assign(this, {});
  }

  $onInit() {
  }

  update() {
    if (this.onUpdate()) {
      this.onUpdate()(this.annotation)
    }
  }

}

/**
 * An AngularJS component that displays a context menu for {@link domain.annotations.Annotation
 * Annotations}.
 *
 * @memberOf admin.studies.components.annotationMenu
 */
const annotationMenuComponent = {
  template: require('./annotationMenu.html'),
  controller: AnnotationMenuController,
  controllerAs: 'vm',
  bindings: {
    annotation: '<',
    onUpdate:   '&'
  }
};

export default ngModule => ngModule.component('annotationMenu', annotationMenuComponent);
