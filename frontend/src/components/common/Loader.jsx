import './loader.module.css'

export const Loader = ({ size = 'md' }) => {
    return <span className={`loader loader-${size}`}></span>;
}
