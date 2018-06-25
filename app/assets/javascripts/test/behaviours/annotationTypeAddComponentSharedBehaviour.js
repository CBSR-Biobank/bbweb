/**
 * Jasmine shared behaviour.
 *
 * @namespace test.behaviours.annotationTypeAddComponentSharedBehaviour
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Context object to share information between the test suite and this shared behaviour.
 *
 * @typedef test.behaviours.annotationTypeAddComponentSharedBehaviour.Context
 * @type object
 *
 * @param {function } createController is a function that creates the component's controller and scope.
 *
 * @param {class} entity is the domain entity to which the annotation type will be added to.
 *
 * @param {string} addAnnotationTypeFuncName is the name of the function on entity which
 * adds the annotation type.
 *
 * @param {string} returnState the state to return after a successful add or when cancel is pressed.
 */

/**
 * Common behaviour for test suites that test {@link domain.annotations.Annotation Annotations}.
 *
 * @function annotationTypeAddComponentSharedBehaviour
 * @memberOf test.behaviours.annotationTypeAddComponentSharedBehaviour
 *
 * @param {test.behaviours.annotationTypeAddComponentSharedBehaviour.Context} context
 */
export default function annotationTypeAddComponentSharedBehaviour(context) {

  describe('(shared) tests', function() {

    beforeEach(function() {
      this.injectDependencies('$q',
                              '$state',
                              'AnnotationType',
                              'modalService',
                              'domainNotificationService',
                              'Factory');

      spyOn(this.$state, 'go').and.returnValue('ok');
    });

    describe('for `submit`', function() {

      it('should change to correct state on a valid submit', function() {
        const annotType = new this.AnnotationType(this.Factory.annotationType());

        spyOn(context.entity.prototype, context.addAnnotationTypeFuncName)
          .and.returnValue(this.$q.when(this.study));

        context.createController();

        this.controller.submit(annotType);
        this.scope.$digest();
        expect(context.entity.prototype[context.addAnnotationTypeFuncName])
          .toHaveBeenCalledWith(annotType);
        expect(this.$state.go).toHaveBeenCalledWith(
          context.returnState, {}, { reload: true });
      });

      it('should fail when annotation type name is a duplicate', function() {
        context.createController();

        spyOn(context.entity.prototype, context.addAnnotationTypeFuncName)
          .and.returnValue(this.$q.reject({
            status: 'error',
            message: 'EntityCriteriaError: annotation type name already used:'
          }));

        spyOn(this.modalService ,'modalOk').and.returnValue(this.$q.when('OK'));

        spyOn(this.domainNotificationService ,'updateErrorModal')
          .and.returnValue(this.$q.when('OK'));

        const annotType = new this.AnnotationType(this.Factory.annotationType());
        this.controller.submit(annotType);
        this.scope.$digest();

        expect(this.modalService.modalOk).toHaveBeenCalled();
        expect(this.domainNotificationService.updateErrorModal).not.toHaveBeenCalled();
        expect(this.$state.go).not.toHaveBeenCalled();
      });

      it('should handle a failure response from the server', function() {
        context.createController();

        spyOn(context.entity.prototype, context.addAnnotationTypeFuncName)
          .and.returnValue(this.$q.reject({
            status: 'error',
            message: 'simulated error'
          }));

        spyOn(this.modalService ,'modalOk').and.returnValue(this.$q.when('OK'));

        spyOn(this.domainNotificationService ,'updateErrorModal')
          .and.returnValue(this.$q.when('OK'));

        const annotType = new this.AnnotationType(this.Factory.annotationType());
        this.controller.submit(annotType);
        this.scope.$digest();

        expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
        expect(this.modalService.modalOk).not.toHaveBeenCalled();
        expect(this.$state.go).not.toHaveBeenCalled();
      });

    });

    it('on cancel, the correct method should be called', function() {
      context.createController();
      this.controller.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(context.returnState, {}, { reload: true });
    });

  });
}
