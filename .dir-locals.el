((nil . ((fill-column . 110)
         (projectile-test-suffix-function . (lambda (project-type) "" "Spec"))
         (eval . (setq eclimd-default-workspace
                       (concat (locate-dominating-file default-directory ".dir-locals.el") "..")))
         (eval . (setq projectile-find-dir-includes-top-level t))
         (eval . (setq-default indent-tabs-mode nil))
         (eval . (global-set-key [f5] 'sbt-command)))))
