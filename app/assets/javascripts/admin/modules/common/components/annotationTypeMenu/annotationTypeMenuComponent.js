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
 * An AngularJS component that
 *
 * @memberOf
 */
const annotationTypeMenuComponent = {
  template: require('./annotationTypeMenu.html'),
  controller: AnnotationTypeMenuController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<',
    allowChanges: '<',
    onView: '&',
    onUpdate: '&',
    onRemove: '&'
  }
};

export default ngModule => ngModule.component('annotationTypeMenu', annotationTypeMenuComponent);
